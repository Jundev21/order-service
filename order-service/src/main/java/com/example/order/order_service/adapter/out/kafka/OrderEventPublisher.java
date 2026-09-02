package com.example.order.order_service.adapter.out.kafka;

import com.example.order.order_service.application.port.out.PublishOrderEventPort;
import com.example.order.order_service.event.OrderCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

// 주문생성이 완료되면 카프카 서버로 데이터 전송한다.
@Component
public class OrderEventPublisher implements PublishOrderEventPort {

    private static String TOPIC = "order-created";
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishOrder(OrderCreatedEvent orderEvent) {
        try {
            kafkaTemplate.send(
                    TOPIC,
                    orderEvent.orderId().toString(),
                    orderEvent
            ).get();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Kafka 이벤트 발행 실패", e
            );
        }
    }
}
