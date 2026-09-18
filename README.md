# Order Service

이커머스 주문 처리 과정에서 발생하는 중복 요청, 비동기 처리 순서, DB와 Kafka 간 데이터 정합성 문제를 직접 구현하며 이해하기 위해 시작한 프로젝트입니다.

주문, 재고, 결제를 각각의 서비스(MSA)로 분리하고 Kafka 이벤트로 연결했습니다. 기능을 구현하는 것뿐만 아니라 실패 상황에서 데이터가 어떻게 달라질 수 있는지 확인하고 이를 보완하는 데 초점을 맞췄습니다.

## 구현하면서 고민한 내용

### 1. 중복 주문 요청 (멱등성)

사용자가 주문 버튼을 여러 번 누르거나 네트워크 문제로 같은 요청이 재전송되면 동일한 주문이 여러 건 생성될 수 있습니다.

이를 방지하기 위해 주문 요청마다 `Idempotency-Key`를 전달하고, 같은 키로 생성된 주문이 있으면 새 주문을 만들지 않고 기존 주문을 반환하도록 구현했습니다.

```text
최초 요청      : 주문 및 Outbox 생성
동일 키 재요청  : 기존 주문 반환
다른 키 일경우 : kafka 이벤트 발급 
```

현재는 애플리케이션에서 기존 주문을 먼저 조회하는 방식입니다.

### 2. 주문 저장과 Kafka 발행의 정합성

처음에는 주문을 저장한 뒤 Kafka 이벤트를 바로 발행하는 방식을 생각했습니다.

하지만 주문 저장은 성공하고 Kafka 발행은 실패하면 주문은 생성됐지만 Product Service에서 재고 처리를 시작하지 못하는 문제가 생길 수 있습니다.

```text
주문 저장 성공
→ Kafka 발행 실패
→ 주문은 존재하지만 재고 처리는 진행되지 않음
```

DB 트랜잭션과 Kafka 발행을 하나의 트랜잭션으로 처리할 수 없기 때문에 Transactional Outbox 패턴을 적용했습니다.

```text
@Transactional
├── 주문 저장
└── Outbox 이벤트 저장(PENDING)

Scheduler
├── PENDING 이벤트 조회
├── Kafka 발행
└── 성공 시 SENT 변경
```

Kafka 발행에 실패하면 이벤트를 PENDING 상태로 남겨 다음 Scheduler 실행에서 다시 처리합니다.

Outbox를 적용해 이벤트 유실 가능성은 줄일 수 있지만, 발행 성공 후 `SENT` 변경 전에 서버가 종료되면 동일 이벤트가 다시 발행될 수 있는 경우도 있어 product-service 에서 event-id 를 기준으로 멱등성 처리도 같이 진행했습니다.

### 3. 비동기 처리 순서

재고 차감과 결제 요청을 동시에 실행하면 재고 처리 결과가 나오기 전에 결제가 진행될 수 있습니다.

그래서 Product Service의 재고 차감 성공 이벤트를 받은 후에만 Payment Service로 결제 요청을 보내도록 처리 순서를 구성했습니다.

```text
주문 생성
→ 재고 차감 요청
→ 재고 차감 성공
→ 결제 요청
→ 결제 결과 처리
```

Order Service가 재고와 결제 결과를 받아 다음 단계를 결정하도록 구성했습니다.

### 4. 결제 실패 보상 처리

현재 구조는 재고를 먼저 차감한 뒤 결제를 진행하는 방식입니다.

따라서 재고 차감 이후 결제에 실패하면 감소한 재고를 다시 복구해야 합니다.

결제 실패 이벤트를 받으면 주문을 `FAILED`로 변경하고, `increase-inventory` 이벤트를 Outbox에 저장합니다. Product Service는 해당 이벤트를 받아 재고를 다시 증가시킵니다.

```text
재고 차감 성공
→ 결제 실패
→ 주문 FAILED
→ 재고 증가 이벤트 발행
→ Product Service 재고 복구
```

각 서비스는 서비스 DB만 로컬 트랜잭션으로 처리하고, 이전 작업을 되돌려야 할 때 보상 이벤트를 발행하는 방식으로 구현했습니다.


### 5. 외부 기술 의존성 분리

헥사고날 아키텍처를 사용하였습니다. 
비즈니스 흐름이 JPA, Kafka, RestClient 구현에 직접 의존하지 않도록 Port와 Adapter 구조로 구성했습니다.

```text
adapter.in
→ application.port.in
→ application.service
→ application.port.out
→ adapter.out
```

Application Service는 인터페이스에 의존하고, 실제 DB 저장과 Kafka 발행은 Adapter에서 처리합니다.

## 주문 처리 흐름

```text
Client : POST /orders
  
Order Service : 상품 정보 조회
  
주문 CREATED + Outbox PENDING 저장 : order-created
  
Product Service
  ├─ 재고 차감 실패 -> 주문 FAILED
  └─ 재고 차감 성공 -> order-payment
     Payment Service
          ├─ 결제 성공 -> 주문 COMPLETED
          └─ 결제 실패 -> (보상 트랜젝션 사용) 주문 FAILED -> 재고 복구 요청
```

## 테스트하면서 확인한 내용

다음 시나리오를 중심으로 단위 테스트를 작성했습니다.

* 같은 `Idempotency-Key`의 주문이 있으면 주문과 Outbox를 다시 저장하지 않는지 확인
* Kafka 발행 성공 시 Outbox가 `SENT`로 변경되는지 확인
* Kafka 발행 실패 시 Outbox를 `SENT`로 변경하지 않는지 확인
* 주문 저장 과정에서 Outbox 이벤트도 함께 생성되는지 확인

## Kafka Topic

| Topic                       | 방향 | 역할                 |
| --------------------------- | -- | ------------------ |
| `order-created`             | 발행 | 재고 차감 요청           |
| `inventory-decreased`       | 수신 | 재고 차감 성공 처리        |
| `inventory-decrease-failed` | 수신 | 재고 차감 실패 처리        |
| `order-payment`             | 발행 | 결제 요청              |
| `success-payment`           | 수신 | 결제 성공 처리           |
| `failed-payment`            | 수신 | 결제 실패 처리           |
| `increase-inventory`        | 발행 | 결제 실패에 따른 재고 복구 요청 |

발행 이벤트의 Kafka Message Key는 `orderId`를 사용합니다.

## 기술 스택

* Java 21
* Spring Boot 4.1
* Spring MVC / RestClient
* Spring Data JPA
* MySQL
* Apache Kafka
* Gradle
* JUnit 5 / Mockito / MockMvc

