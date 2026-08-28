package com.example.order.order_service.event;

public record InventoryDecreasedEvent(
        Long orderId,
        Long goodsId,
        int quantity
) {
}