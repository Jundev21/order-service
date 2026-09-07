# Order Service

## 프로젝트 목적

현재 이커머스 백엔드 기능을 계속 추가하며 학습 중인 프로젝트입니다.

이커머스 백엔드의 주문 처리 흐름을 공부하기 위해 만든 프로젝트입니다.

단순 CRUD 구현보다는 MSA 환경에서 주문 서비스와 상품 서비스가 어떻게 데이터를 주고받는지, Kafka를 이용한 비동기 처리와 주문 상태 변경, 중복 요청 및 이벤트 처리, DB와 Kafka 사이의 데이터 정합성 문제를 직접 구현하면서 공부하는 것을 목표로 했습니다.

* Hexagonal Architecture
* 주문 생성 및 상태 관리
* JPA를 이용한 주문 저장
* Kafka Producer / Consumer
* 서비스 간 비동기 이벤트 처리
* API / Kafka 이벤트 멱등성
* Transactional Outbox
* 단위 테스트

---

## 기술 스택

* Java 21
* Spring Boot
* Spring Data JPA
* MySQL
* Apache Kafka
* JUnit5
* Mockito
* Gradle

---

## 주문 처리 흐름

사용자가 주문을 요청하면 Order Service에서 주문을 먼저 `CREATED` 상태로 저장합니다.

주문을 저장하면서 Kafka로 전달할 `OrderCreatedEvent` 정보도 Outbox 테이블에 `PENDING` 상태로 함께 저장합니다.

```text
Client
  ↓
POST /orders
  ↓
Order Service
  ↓
@Transactional
  ├─ Order 저장 (CREATED)
  └─ Outbox 저장 (PENDING)
```

이후 Scheduler가 주기적으로 Outbox 테이블의 `PENDING` 이벤트를 조회하여 Kafka로 전달합니다.

```text
Scheduler
   ↓
PENDING 이벤트 조회
   ↓
order-created
   ↓
Kafka
   ↓
Product Service
   ↓
재고 차감
```

Product Service에서 재고 처리 결과를 다시 Kafka로 전달합니다.

```text
재고 차감 성공
→ inventory-decreased

재고 차감 실패
→ inventory-decrease-failed
```

Order Service에서는 두 이벤트를 Consumer로 받아 주문 상태를 변경합니다.

```text
inventory-decreased
        ↓
COMPLETED


inventory-decrease-failed
        ↓
FAILED
```

현재 주문 상태는 아래와 같이 관리합니다.

```text
CREATED
COMPLETED
FAILED
CANCELED
```

---

## 전체 구조

```text
Client
  ↓
POST /orders
  ↓
OrderController
  ↓
CreateOrderUseCase
  ↓
CreateOrderService
  ↓
@Transactional
  ├─ Order 저장 (CREATED)
  └─ Outbox Event 저장 (PENDING)
            ↓
        Transaction Commit
            ↓
      Outbox Scheduler
            ↓
      PENDING 이벤트 조회
            ↓
          Kafka
       order-created
            ↓
      Product Service
            ↓
         재고 차감
            ↓
┌─────────────────────────────┐
│                             │
성공                          실패
│                             │
inventory-decreased           inventory-decrease-failed
│                             │
└──────────────┬──────────────┘
               ↓
             Kafka
               ↓
         Order Service
               ↓
          주문 상태 변경
          ↓          ↓
    COMPLETED       FAILED
```

---

## Hexagonal Architecture

Controller, Kafka, DB와 같은 외부 영역이 Application 로직에 직접 섞이지 않도록 Port / Adapter 구조로 구성했습니다.

```text
adapter.in
    ↓
port.in
    ↓
application.service
    ↓
port.out
    ↓
adapter.out
```

주요 구조는 아래와 같습니다.

```text
adapter
├── in
│   ├── web
│   │   └── OrderController
│   │
│   ├── kafka
│   │   └── InventoryEventConsumer
│   │
│   └── scheduler
│       └── OutboxScheduler
│
└── out
    ├── persistence
    │   ├── OrderPersistenceAdapter
    │   └── OutboxAdapter
    │
    └── kafka
        └── OrderEventPublisher

application
├── port
│   ├── in
│   └── out
│
└── service
    ├── CreateOrderService
    ├── ConsumerProductEventService
    └── OutboxPublishService

domain
└── model
    ├── Order
    └── OrderStatus
```

예를 들어 `CreateOrderService`에서는 `JpaRepository`나 `KafkaTemplate`을 직접 사용하지 않고 Port를 통해 외부 영역에 접근하도록 했습니다.

주문 생성 시에는 Kafka에 직접 이벤트를 전달하지 않고 Outbox에 이벤트를 저장합니다.

```text
CreateOrderService
   │
   ├─ SaveOrderPort
   │      ↓
   │  OrderPersistenceAdapter
   │
   └─ OutboxEventPort
          ↓
      OutboxAdapter
```

Kafka 발행은 별도의 Outbox Publisher가 담당합니다.

```text
OutboxScheduler
       ↓
OutboxPublishService
       ↓
PublishOrderEventPort
       ↓
OrderEventPublisher
       ↓
Kafka
```

---

## Kafka Topic

| Topic                       | 역할                             |
| --------------------------- | ------------------------------ |
| `order-created`             | 주문 생성 이벤트를 Product Service에 전달 |
| `inventory-decreased`       | 재고 차감 성공 결과 전달                 |
| `inventory-decrease-failed` | 재고 차감 실패 결과 전달                 |

Kafka Message Key는 `orderId`를 사용합니다.

---

# 멱등성 처리

주문 처리 과정에서는 동일한 요청이나 동일한 이벤트가 여러 번 처리될 수 있기 때문에 API 요청과 Kafka 이벤트에 각각 멱등성을 적용했습니다.

## API 중복 요청

사용자가 주문 버튼을 여러 번 클릭하거나 네트워크 오류로 같은 요청이 다시 전달될 수 있습니다.

이 경우 동일한 주문이 여러 개 생성될 수 있기 때문에 요청 Header에 `Idempotency-Key`를 전달하도록 했습니다.

```text
POST /orders
Idempotency-Key: abc-123
```

Order Service에서는 같은 Key로 생성된 주문이 있는지 먼저 확인합니다.

```text
첫 요청
Idempotency-Key = abc-123
        ↓
기존 주문 없음
        ↓
주문 생성
Outbox 이벤트 생성


동일 Key 재요청
Idempotency-Key = abc-123
        ↓
기존 주문 존재
        ↓
기존 주문 반환
        ↓
새 주문 생성 X
새 Outbox 이벤트 생성 X
```

`idempotency_key`에는 UNIQUE 제약조건도 적용하여 동시에 같은 Key를 가진 요청이 들어오는 경우 DB에서도 중복 저장을 방지하도록 했습니다.

---

## Kafka 중복 이벤트

Kafka에서는 동일한 이벤트가 다시 전달될 가능성이 있습니다.

Consumer가 메시지를 처리한 이후 Offset Commit 전에 장애가 발생할 수도 있고, Outbox에서도 Kafka 발행 성공 후 `SENT` 상태로 변경하기 전에 서버가 종료될 수 있습니다.

```text
Kafka 발행 성공
        ↓
서버 장애
        ↓
Outbox는 아직 PENDING
        ↓
서버 재시작
        ↓
동일 이벤트 재발행
```

이 경우 Product Service에서 같은 재고 차감 이벤트가 두 번 처리되면 실제 재고도 두 번 감소할 수 있습니다.

이를 방지하기 위해 이벤트마다 `eventId`를 생성합니다.

```text
OrderCreatedEvent

eventId
orderId
goodsId
quantity
```

Product Service에서는 처리한 `eventId`를 별도 테이블에 저장합니다.

같은 `eventId`를 가진 이벤트가 다시 들어오면 이미 처리한 이벤트로 판단하고 재고를 다시 차감하지 않습니다.

```text
eventId = event-123

첫 수신
→ 처리 기록 없음
→ 재고 차감
→ eventId 저장


동일 eventId 재수신
→ 처리 기록 존재
→ 재고 차감 X
```

API 요청의 중복은 `Idempotency-Key`, Kafka 이벤트의 중복 처리는 `eventId`를 기준으로 구분했습니다.

```text
Idempotency-Key
→ HTTP 요청 중복 방지

eventId
→ Kafka 이벤트 중복 처리 방지
```

---

# Transactional Outbox

## Outbox를 사용하는 이유

기존에는 주문 정보를 DB에 저장한 이후 Kafka로 `OrderCreatedEvent`를 바로 전달하는 구조였습니다.

```text
Order 저장
   ↓
Kafka 발행
```

그런데 다음과 같은 문제가 발생할 수 있습니다.

```text
Order DB 저장 성공 ✅
        ↓
Kafka 이벤트 발행 실패 ❌
```

이 경우 Order Service에는 주문이 생성되어 있지만 Product Service는 주문 생성 사실을 전달받지 못하기 때문에 재고 차감이 이루어지지 않습니다.

DB 트랜잭션은 DB 내부 작업에 대해서는 원자성을 보장하지만 MySQL과 Kafka는 서로 다른 시스템이기 때문에 일반적인 DB 트랜잭션 하나로 같이 묶을 수 없습니다.

이 문제를 해결하기 위해 Transactional Outbox 패턴을 적용했습니다.

---

## Outbox 처리 방식

주문을 저장한 후 바로 Kafka로 이벤트를 보내는 대신, 주문 정보와 Kafka로 전달할 이벤트 정보를 같은 DB 트랜잭션 안에서 저장합니다.

```text
@Transactional

Order 저장
+
Outbox Event 저장

↓
Commit
```

따라서 주문 저장이나 Outbox 저장 중 하나라도 실패하면 전체 트랜잭션이 Rollback 됩니다.

Outbox에는 Kafka로 전달할 이벤트 정보를 저장합니다.

```text
eventId
eventType
payload
status
```

새로운 이벤트는 처음에 `PENDING` 상태로 저장합니다.

```text
Order 저장
+
Outbox

eventId = event-123
eventType = ORDER_CREATED
payload = {...}
status = PENDING
```

---

## Outbox Scheduler

Outbox 이벤트를 저장한 이후 별도의 Scheduler가 주기적으로 `PENDING` 상태의 이벤트를 조회합니다.

```text
Scheduler
   ↓
PENDING 조회
   ↓
Kafka 발행
```

Kafka 발행에 성공하면 해당 Outbox 이벤트를 `SENT` 상태로 변경합니다.

```text
PENDING
   ↓
Kafka 발행 성공
   ↓
SENT
```

Kafka 발행에 실패하면 `SENT`로 변경하지 않습니다.

따라서 Outbox 이벤트는 계속 `PENDING` 상태로 남고 다음 Scheduler 실행 시 다시 Kafka 전송을 시도할 수 있습니다.

```text
PENDING
   ↓
Kafka 발행 실패
   ↓
PENDING 유지
   ↓
다음 Scheduler
   ↓
재전송
```

이를 통해 Order 데이터는 저장됐지만 Kafka 이벤트가 사라지는 문제를 줄일 수 있습니다.

---

## Outbox 조회 최적화

처음에는 모든 `PENDING` 이벤트를 조회하는 방식으로 구현했지만 데이터가 많아질 경우 한 번에 너무 많은 데이터를 조회하는 문제가 생길 수 있습니다.

따라서 오래된 `PENDING` 이벤트부터 최대 100개씩 조회하도록 변경했습니다.

```sql
SELECT *
FROM outbox_events
WHERE status = 'PENDING'
ORDER BY id ASC
    LIMIT 100;
```

Spring Data JPA에서는 다음과 같은 방식으로 사용했습니다.

```java
findTop100ByStatusOrderByIdAsc(
        OutboxStatus.PENDING
        );
```

한 번의 Scheduler 실행에서 최대 100개의 이벤트만 처리합니다.

```text
Scheduler 1회
↓
PENDING 100개
↓
Kafka 발행
↓
SENT

다음 Scheduler
↓
다음 PENDING 100개
```

---

## Outbox Index

Scheduler에서는 반복적으로 다음 조건을 사용합니다.

```text
status = PENDING
+
id ASC
```

Outbox 데이터가 많아질수록 매번 테이블 전체를 확인하면 조회 비용이 증가하기 때문에 `(status, id)` 복합 인덱스를 적용했습니다.

```sql
CREATE INDEX idx_outbox_status_id
    ON outbox_events(status, id);
```

`(status, id)`는 인덱스 두 개가 아니라 두 컬럼을 사용하는 하나의 복합 인덱스입니다.

```text
1순위 → status
2순위 → id
```

개념적으로는 다음과 같은 순서로 관리됩니다.

```text
PENDING, 1
PENDING, 5
PENDING, 10
PENDING, 20

SENT, 2
SENT, 3
SENT, 4
```

따라서 Scheduler가 `PENDING` 이벤트를 조회할 때 해당 영역을 찾고, 같은 `PENDING` 안에서는 `id` 순서대로 데이터를 읽을 수 있습니다.

```text
PENDING 시작점
     ↓
id 순서대로 조회
     ↓
100개
     ↓
종료
```

`LIMIT 100`은 한 번에 가져오는 데이터의 양을 제한하고, `(status, id)` 인덱스는 필요한 데이터를 빠르게 찾기 위해 사용했습니다.

```text
LIMIT
→ 조회하는 데이터 개수 제한

INDEX
→ 조회할 데이터 탐색 비용 감소
```

---

## Outbox와 멱등성

Outbox를 사용한다고 해서 Kafka 이벤트가 반드시 한 번만 전달되는 것은 아닙니다.

예를 들어 Kafka 발행에는 성공했지만 Outbox를 `SENT`로 변경하기 전에 서버가 종료될 수 있습니다.

```text
Outbox PENDING
     ↓
Kafka 발행 성공
     ↓
서버 종료
     ↓
SENT 변경 실패
```

서버가 다시 실행되면 Scheduler는 해당 이벤트를 여전히 `PENDING`으로 판단하고 같은 이벤트를 다시 Kafka로 전달합니다.

```text
서버 재시작
↓
PENDING 조회
↓
동일 eventId Kafka 재발행
```

따라서 Consumer에서는 `eventId`를 이용한 멱등성 처리가 필요합니다.

```text
Outbox
→ Kafka 이벤트 유실 방지

eventId 멱등성
→ Kafka 이벤트 중복 처리 방지
```

---

# 구현하면서 공부한 내용

## Kafka Consumer Group / Offset

Consumer Group이 마지막으로 처리한 Offset을 기준으로 메시지를 이어서 소비하는 구조를 확인했습니다.

개발 중 `earliest`, `latest` 설정에 따라 이전 Kafka 메시지가 다시 소비되는 경우도 직접 확인했습니다.

## Kafka 역직렬화

Order Service에서 서로 다른 Kafka 이벤트를 Consumer로 받으면서 JSON 역직렬화와 Message Converter 동작을 확인했습니다.

## 서비스 간 데이터 처리

Order Service에서 Product DB에 직접 접근하지 않고 Kafka 이벤트를 통해 재고 처리를 요청하도록 구성했습니다.

```text
Order Service
→ 주문 관리

Product Service
→ 상품 / 재고 관리
```

각 서비스가 자신의 DB와 기능을 관리하도록 나누었습니다.

## 주문 상태 변경

주문을 생성했다고 바로 완료 상태로 변경하지 않고 Product Service의 재고 처리 결과를 받은 이후 최종 상태를 변경하도록 했습니다.

```text
CREATED
   ↓
재고 처리
   ↓
COMPLETED / FAILED
```

## DB와 Kafka 정합성

DB 저장과 Kafka 발행은 서로 다른 시스템에서 수행되기 때문에 `@Transactional`만으로 두 작업의 원자성을 보장할 수 없다는 점을 확인했습니다.

Transactional Outbox를 적용하여 주문 저장과 이벤트 저장을 같은 DB 트랜잭션으로 처리하고, 실제 Kafka 발행은 별도의 Publisher가 담당하도록 분리했습니다.

---

# 테스트

구현한 기능을 확인하기 위해 각 영역별 테스트를 작성했습니다.

* Order Domain Test
* CreateOrderService Test
* OrderPersistenceAdapter Test
* OrderController Test
* OutboxPublishService Test
* OutboxAdapter Test

Outbox 테스트에서는 다음 내용을 확인했습니다.

```text
Kafka 발행 성공
→ markAsSent 호출

PENDING 이벤트 조회
→ PENDING 상태 이벤트 반환

markAsSent
→ Outbox 상태 SENT 변경
```

---

# 현재 구현 범위

* [x] 주문 생성 API
* [x] Request Validation
* [x] 주문 MySQL 저장
* [x] Hexagonal Architecture 적용
* [x] Kafka Producer / Consumer
* [x] Product Service 재고 차감 요청
* [x] 재고 차감 성공 이벤트 수신
* [x] 재고 차감 실패 이벤트 수신
* [x] 성공 시 주문 `COMPLETED` 처리
* [x] 실패 시 주문 `FAILED` 처리
* [x] `Idempotency-Key`를 이용한 주문 중복 생성 방지
* [x] `eventId`를 이용한 Kafka 이벤트 중복 처리 방지
* [x] Transactional Outbox 적용
* [x] Outbox Scheduler 기반 Kafka 발행
* [x] Outbox `PENDING` / `SENT` 상태 관리
* [x] Outbox 100건 단위 조회
* [x] Outbox `(status, id)` 복합 인덱스 적용
* [x] 기본 단위 테스트
