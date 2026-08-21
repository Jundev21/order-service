package com.example.order.order_service.application.port.out;

import com.example.order.order_service.domain.model.Order;
import com.example.order.order_service.event.OrderCreatedEvent;

public interface PublishOrderEventPort {
    void publishOrder(OrderCreatedEvent orderEvent);
}
