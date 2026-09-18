package com.example.order.order_service.adapter.out.kafka;

import com.example.order.order_service.application.port.out.PublishOrderEventPort;
import com.example.order.order_service.event.OrderCreatedEvent;
import com.example.order.order_service.event.PaymentFailedEvent;
import com.example.order.order_service.event.RequestPaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher implements PublishOrderEventPort {

    private static final String TOPIC = "order-created";
    private static final String PAYMENT_TOPIC = "order-payment";
    private static final String INCREASE_INVENTORY_TOPIC = "increase-inventory";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishOrder(OrderCreatedEvent event) {
        send(TOPIC, event.orderId().toString(), event);
    }

    @Override
    public void publishPaymentOrder(RequestPaymentEvent event) {
        send(PAYMENT_TOPIC, event.orderId().toString(), event);
    }

    @Override
    public void increaseInventory(PaymentFailedEvent event) {
        send(INCREASE_INVENTORY_TOPIC, event.orderId().toString(), event);
    }

    private void send(String topic, String key, Object event) {
        try {
            kafkaTemplate.send(topic, key, event).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Kafka 이벤트 발행 중 인터럽트 발생", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Kafka 이벤트 발행 실패. topic=" + topic, e);
        }
    }
}
