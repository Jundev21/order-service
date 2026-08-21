package com.example.order.order_service.adapter.in.web;

import com.example.order.order_service.domain.model.OrderStatus;

public record CreateOrderResponse(
        Long orderId,
        OrderStatus orderStatus
) {
}
