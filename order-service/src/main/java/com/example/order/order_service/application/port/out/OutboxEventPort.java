package com.example.order.order_service.application.port.out;

import com.example.order.order_service.application.port.out.dto.PendingOutboxEvent;

import java.util.List;

public interface OutboxEventPort {
    void save(String eventId, String eventType, String payload);
    List<PendingOutboxEvent> findPendingEvents();
    void markAsSent(Long id);
}