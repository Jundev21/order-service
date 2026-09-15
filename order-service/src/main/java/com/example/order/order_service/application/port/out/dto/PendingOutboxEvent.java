package com.example.order.order_service.application.port.out.dto;

public record PendingOutboxEvent(
        Long id,
        String payload
) {
}
