# Order Service

## 프로젝트 목적

현재 이커머스 백엔드 기능을 계속 추가하며 학습 중인 프로젝트입니다.

이커머스 백엔드의 주문 처리 흐름을 공부하기 위해 만든 프로젝트입니다.

단순 CRUD 구현보다는 MSA 환경에서 주문 서비스와 상품 서비스가 어떻게 데이터를 주고받는지, Kafka를 이용한 비동기 처리와 주문 상태 변경은 어떻게 하는지 직접 구현해보는 것을 목표로 했습니다.

* Hexagonal Architecture
* 주문 생성 및 상태 관리
* JPA를 이용한 주문 저장
* Kafka Producer / Consumer
* 서비스 간 비동기 이벤트 처리
* API / Kafka 이벤트 멱등성
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

주문 저장이 완료되면 `OrderCreatedEvent`를 Kafka로 발행하고 Product Service에서 해당 이벤트를 받아 재고를 처리합니다.

```text
Client
  ↓
POST /orders
  ↓
Order Service
  ↓
주문 저장
CREATED
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
주문 요청
   ↓
OrderController
   ↓
CreateOrderUseCase
   ↓
CreateOrderService
   ↓
Order 저장
   ↓
OrderCreatedEvent
   ↓
Kafka
   ↓
Product Service
   ↓
재고 차감
   ↓
┌───────────────────────┐
│                       │
성공                    실패
│                       │
inventory-decreased     inventory-decrease-failed
│                       │
└───────────┬───────────┘
            ↓
          Kafka
            ↓
      Order Service
            ↓
       주문 상태 변경
       ↓         ↓
 COMPLETED     FAILED
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
│   └── kafka
│       └── InventoryEventConsumer
│
└── out
    ├── persistence
    │   ├── OrderPersistenceAdapter
    │   └── InventoryConsumerAdapter
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
    └── ConsumerProductEventService

domain
└── model
    ├── Order
    └── OrderStatus
```

예를 들어 `CreateOrderService`에서는 `JpaRepository`, `KafkaTemplate`을 직접 사용하지 않고 각각 Port를 통해 접근하도록 했습니다.

```text
CreateOrderService
   │
   ├─ SaveOrderPort
   │      ↓
   │  Persistence Adapter
   │
   └─ PublishOrderEventPort
          ↓
      Kafka Adapter
```

---

## Kafka Topic

| Topic                       | 역할                          |
| --------------------------- | --------------------------- |
| `order-created`             | 주문 생성 후 Product Service에 전달 |
| `inventory-decreased`       | 재고 차감 성공                    |
| `inventory-decrease-failed` | 재고 차감 실패                    |

Kafka Message Key는 `orderId`를 사용합니다.

---

## 멱등성 처리

주문 처리 과정에서 동일한 요청이나 이벤트가 여러 번 처리되는 문제도 같이 공부했습니다.

### API 중복 요청

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
        ↓
Kafka 이벤트 발행


동일 Key 재요청
Idempotency-Key = abc-123
        ↓
기존 주문 존재
        ↓
기존 주문 반환
        ↓
새 주문 생성 X
Kafka 재발행 X
```

`idempotency_key`에는 UNIQUE 제약조건을 적용하여 동시에 같은 요청이 들어오는 경우도 중복 저장되지 않도록 했습니다.

---

### Kafka 중복 이벤트

Kafka Consumer가 메시지를 처리한 이후 Offset Commit 전에 장애가 발생하면 동일한 메시지를 다시 소비할 수 있습니다.

재고 차감 같은 작업이 중복으로 처리되면 실제 재고가 두 번 감소할 수 있기 때문에 이벤트마다 `eventId`를 생성했습니다.

```text
OrderCreatedEvent

eventId
orderId
goodsId
quantity
```

Product Service에서는 처리한 `eventId`를 별도 테이블에 저장하고, 같은 이벤트가 다시 들어오면 처리하지 않도록 했습니다.

```text
eventId = event-123

첫 수신
→ 처리 기록 없음
→ 재고 차감
→ eventId 저장

동일 eventId 재수신
→ 이미 처리된 이벤트
→ 재고 차감 X
```

API 요청의 중복은 `Idempotency-Key`, Kafka 메시지의 중복 처리는 `eventId`를 기준으로 구분했습니다.

---

## 구현하면서 공부한 내용

### Kafka Consumer Group / Offset

Consumer Group이 마지막으로 처리한 Offset을 기준으로 메시지를 이어서 소비하는 구조를 확인했습니다.

개발 중 `earliest`, `latest` 설정에 따라 이전 Kafka 메시지가 다시 소비되는 경우도 직접 확인했습니다.

### Kafka 역직렬화

Order Service에서 서로 다른 Kafka 이벤트를 Consumer로 받으면서 JSON 역직렬화와 Message Converter 동작을 확인했습니다.

### 서비스 간 데이터 처리

Order Service에서 Product DB에 직접 접근하지 않고 Kafka 이벤트를 통해 재고 처리를 요청하도록 구성했습니다.

```text
Order Service
→ 주문 관리

Product Service
→ 상품 / 재고 관리
```

각 서비스가 자신의 DB와 기능을 관리하도록 나누었습니다.

### 주문 상태 변경

주문을 생성했다고 바로 완료 상태로 변경하지 않고 Product Service의 재고 처리 결과를 받은 이후 최종 상태를 변경하도록 했습니다.

```text
CREATED
   ↓
재고 처리
   ↓
COMPLETED / FAILED
```

---

## 테스트

구현한 기능을 확인하기 위해 각 영역별 테스트를 작성했습니다.

* Order Domain Test
* CreateOrderService Test
* OrderPersistenceAdapter Test
* OrderController Test

---

## 현재 구현 범위

* [x] 주문 생성 API
* [x] Request Validation
* [x] 주문 MySQL 저장
* [x] Hexagonal Architecture 적용
* [x] 주문 생성 Kafka 이벤트 발행
* [x] Product Service 재고 차감 요청
* [x] 재고 차감 성공 이벤트 수신
* [x] 재고 차감 실패 이벤트 수신
* [x] 성공 시 주문 `COMPLETED` 처리
* [x] 실패 시 주문 `FAILED` 처리
* [x] `Idempotency-Key`를 이용한 주문 중복 생성 방지
* [x] `eventId`를 이용한 Kafka 이벤트 중복 처리 방지
* [x] 기본 단위 테스트

