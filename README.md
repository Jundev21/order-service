# Order Service

## 프로젝트 목적

이커머스에 관련된 주문 처리 흐름과 MSA 환경에서의 이벤트 기반 통신을 직접 구현해보기 위해 만든 프로젝트입니다.

단순 CRUD보다는 아래 내용을 직접 구현하고 이해하는 것을 목표로 했습니다.

* Hexagonal Architecture
* 주문 도메인 설계
* JPA 기반 주문 저장
* Kafka Producer / Consumer
* 서비스 간 비동기 이벤트 처리
* 주문 상태 관리
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

## 주요 기능

### 주문 생성

```text
Client
  ↓
POST /orders
  ↓
Order Service
  ↓
주문 CREATED 상태로 저장
  ↓
OrderCreatedEvent 발행
```

주문 생성 시 Order Service에서 재고를 직접 처리하지 않고 Kafka를 통해 Product Service에 주문 생성 사실을 전달합니다.

---

## 재고 처리 결과 반영

Product Service에서 재고 차감을 처리한 후 결과를 Kafka 이벤트로 전달합니다.

```text
Order Service
    │
    │ order-created
    ▼
Kafka
    │
    ▼
Product Service
    │
    ├─ 재고 차감 성공
    │      ↓
    │ inventory-decreased
    │
    └─ 재고 차감 실패
           ↓
      inventory-decrease-failed
```

Order Service는 결과 이벤트를 다시 Consumer로 받아 주문 상태를 변경합니다.

```text
inventory-decreased
        ↓
COMPLETED

inventory-decrease-failed
        ↓
FAILED
```

현재 주문 상태는 다음과 같이 관리합니다.

```text
CREATED
COMPLETED
FAILED
CANCELED
```

---

## 전체 처리 흐름

```text
주문 요청
   ↓
Order Controller
   ↓
CreateOrderUseCase
   ↓
CreateOrderService
   ↓
Order 저장
status = CREATED
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

외부 기술에 Application 로직이 직접 의존하지 않도록 Port / Adapter 구조로 구성했습니다.

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

현재 주요 구조는 다음과 같습니다.

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

예를 들어 주문 생성 서비스에서는 `JpaRepository`나 `KafkaTemplate`을 직접 사용하지 않습니다.

```text
CreateOrderService
   │
   ├─ SaveOrderPort
   │       ↓
   │   Persistence Adapter
   │
   └─ PublishOrderEventPort
           ↓
       Kafka Adapter
```

이를 통해 Application 영역과 DB / Kafka 같은 외부 기술의 책임을 분리했습니다.

---

## Kafka Topic

| Topic                       | 역할                          |
| --------------------------- | --------------------------- |
| `order-created`             | 주문 생성 후 Product Service에 전달 |
| `inventory-decreased`       | 재고 차감 성공                    |
| `inventory-decrease-failed` | 재고 차감 실패                    |

Kafka Message Key는 `orderId`를 사용합니다.

---

## 구현하면서 공부한 내용

### Kafka Consumer Group / Offset

Consumer Group이 마지막으로 처리한 offset을 기준으로 메시지를 이어서 소비하는 구조와 `earliest`, `latest` 설정 차이를 확인했습니다.

### Kafka 역직렬화

서로 다른 이벤트 타입을 하나의 서비스에서 Consumer로 받으면서 JSON 역직렬화 방식과 Message Converter의 동작을 확인했습니다.

### 서비스 간 책임 분리

Order Service에서 Product DB에 직접 접근하지 않고 이벤트를 통해 재고 처리를 요청하도록 구성했습니다.

```text
Order Service → 주문 관리
Product Service → 상품 / 재고 관리
```

### 주문 상태 관리

주문 생성 이후 다른 서비스의 처리 결과에 따라 최종 주문 상태가 결정되도록 구현했습니다.

```text
CREATED
   ↓
재고 처리
   ↓
COMPLETED / FAILED
```

---

## 테스트

주문 처리 과정의 각 영역을 분리해서 확인하기 위해 테스트를 작성했습니다.

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
* [x] 기본 단위 테스트
