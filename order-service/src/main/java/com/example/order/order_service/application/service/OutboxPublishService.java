package com.example.order.order_service.application.service;

import com.example.order.order_service.adapter.out.outbox.payment.PaymentOutboxEventEntity;
import com.example.order.order_service.adapter.out.outbox.product.OutboxEventEntity;
import com.example.order.order_service.application.port.out.PaymentOutboxEventPort;
import com.example.order.order_service.application.port.out.PublishOrderEventPort;
import com.example.order.order_service.application.port.out.OutboxEventPort;
import com.example.order.order_service.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxPublishService {

    private final OutboxEventPort outboxEventPort;
    private final PaymentOutboxEventPort paymentOutboxEventPort;
    private final PublishOrderEventPort publishOrderEventPort;
    private final ObjectMapper objectMapper;

    public void publishPendingEvents() {

        List<OutboxEventEntity> events = outboxEventPort.findPendingEvents();

        for (OutboxEventEntity outboxEvent : events) {
            OrderCreatedEvent event = objectMapper.readValue(
                    outboxEvent.getPayload(),
                    OrderCreatedEvent.class
            );
            publishOrderEventPort.publishOrder(event);
            outboxEventPort.markAsSent(outboxEvent.getId());
        }
    }

    public void publishPaymentEvents() {

        List<PaymentOutboxEventEntity> events = paymentOutboxEventPort.findPendingEvents();

        for (PaymentOutboxEventEntity outboxEvent : events) {
            OrderCreatedEvent event = objectMapper.readValue(
                    outboxEvent.getPayload(),
                    OrderCreatedEvent.class
            );
            publishOrderEventPort.publishPaymentOrder(event);
            paymentOutboxEventPort.markAsSent(outboxEvent.getId());
        }
    }
}