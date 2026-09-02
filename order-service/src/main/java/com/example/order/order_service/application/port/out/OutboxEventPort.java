package com.example.order.order_service.application.port.out;

import com.example.order.order_service.adapter.out.outbox.OutboxEventEntity;

import java.util.List;

public interface OutboxEventPort {
    void save(String eventId, String eventType, String payload);
    List<OutboxEventEntity> findPendingEvents();
    void markAsSent(Long id);
}