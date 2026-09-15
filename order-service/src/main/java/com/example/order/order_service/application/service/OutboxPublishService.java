package com.example.order.order_service.application.service;

import com.example.order.order_service.application.port.out.PaymentOutboxEventPort;
import com.example.order.order_service.application.port.out.PublishOrderEventPort;
import com.example.order.order_service.application.port.out.OutboxEventPort;
import com.example.order.order_service.application.port.out.dto.PendingOutboxEvent;
import com.example.order.order_service.event.OrderCreatedEvent;
import com.example.order.order_service.event.RequestPaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class OutboxPublishService {

    private final OutboxEventPort outboxEventPort;
    private final PaymentOutboxEventPort paymentOutboxEventPort;
    private final PublishOrderEventPort publishOrderEventPort;
    private final ObjectMapper objectMapper;

    public void publishOrderCreatedEvents() {
        publish(
                outboxEventPort.findPendingEvents(),
                OrderCreatedEvent.class,
                publishOrderEventPort::publishOrder,
                outboxEventPort::markAsSent
        );
    }

    public void publishPaymentRequestedEvents() {
        publish(
                paymentOutboxEventPort.findPendingEvents(),
                RequestPaymentEvent.class,
                publishOrderEventPort::publishPaymentOrder,
                paymentOutboxEventPort::markAsSent
        );
    }

    private <T> void publish(
            List<PendingOutboxEvent> events,
            Class<T> eventType,
            Consumer<T> publisher,
            Consumer<Long> sentMarker
    ) {
        for (PendingOutboxEvent outboxEvent : events) {
            T event = objectMapper.readValue(outboxEvent.payload(), eventType);
            publisher.accept(event);
            sentMarker.accept(outboxEvent.id());
        }
    }
}