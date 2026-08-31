package com.example.order.order_service.application.port.out;

import com.example.order.order_service.domain.model.OrderStatus;

public interface InventoryConsumerEventPort {
    void changeOrderStatus(Long orderId, OrderStatus orderStatus);
}
