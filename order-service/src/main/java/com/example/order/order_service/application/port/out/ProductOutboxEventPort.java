package com.example.order.order_service.application.port.out;

import com.example.order.order_service.application.port.out.dto.PendingOutboxEvent;

import java.util.List;

public interface ProductOutboxEventPort {
    void save(String eventId, String eventType, String payload);
    List<PendingOutboxEvent> findPendingEvents(String eventType);
    void markAsSent(Long id);
}