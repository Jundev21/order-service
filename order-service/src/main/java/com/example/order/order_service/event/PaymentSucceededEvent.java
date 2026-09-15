package com.example.order.order_service.event;

public record PaymentSucceededEvent(
        Long orderId,
        Long amount
) {
}