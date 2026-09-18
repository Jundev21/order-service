package com.example.order.order_service.event;

public record PaymentFailedEvent(
        Long orderId,
        Long goodsId,
        Long amount,
        int quantity,
        String reason
) {
}