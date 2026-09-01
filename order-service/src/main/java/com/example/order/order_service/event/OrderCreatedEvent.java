package com.example.order.order_service.event;

import com.example.order.order_service.domain.model.Order;
import com.example.order.order_service.domain.model.OrderStatus;

public record OrderCreatedEvent(
        String eventId,
        Long orderId,
        Long goodsId,
        int quantity,
        OrderStatus status
) {
    public static OrderCreatedEvent from(
            Order order, String eventId
    ) {
        return new OrderCreatedEvent(
                eventId,
                order.getId(),
                order.getGoodsId(),
                order.getQuantity(),
                order.getOrderStatus()
        );
    }
}
