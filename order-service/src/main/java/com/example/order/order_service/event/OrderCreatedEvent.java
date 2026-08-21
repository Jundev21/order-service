package com.example.order.order_service.event;

import com.example.order.order_service.domain.model.Order;
import com.example.order.order_service.domain.model.OrderStatus;

public record OrderCreatedEvent(
        Long orderId,
        Long goodsId,
        int quantity,
        OrderStatus status
) {
    public static OrderCreatedEvent from(
            Order order
    ) {
        return new OrderCreatedEvent(
                order.getId(),
                order.getGoodsId(),
                order.getQuantity(),
                order.getOrderStatus()
        );
    }
}
