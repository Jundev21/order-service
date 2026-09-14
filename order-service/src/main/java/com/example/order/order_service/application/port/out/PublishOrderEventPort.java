package com.example.order.order_service.application.port.out;

import com.example.order.order_service.event.OrderCreatedEvent;
import com.example.order.order_service.event.RequestPaymentEvent;

public interface PublishOrderEventPort {
    void publishOrder(OrderCreatedEvent orderEvent);
    void publishPaymentOrder(RequestPaymentEvent orderEvent);
}
