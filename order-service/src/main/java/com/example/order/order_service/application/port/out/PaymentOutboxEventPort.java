package com.example.order.order_service.application.port.out;

import com.example.order.order_service.adapter.out.outbox.payment.PaymentOutboxEventEntity;
import com.example.order.order_service.application.port.out.dto.PendingOutboxEvent;

import java.util.List;

public interface PaymentOutboxEventPort {
    void save(String eventId, String eventType, String payload);
    List<PendingOutboxEvent> findPendingEvents();
    void markAsSent(Long id);
}