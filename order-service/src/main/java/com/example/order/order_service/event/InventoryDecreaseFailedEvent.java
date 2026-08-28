package com.example.order.order_service.event;

public record InventoryDecreaseFailedEvent(
        Long orderId,
        Long goodsId,
        int quantity,
        String reason
) {
}