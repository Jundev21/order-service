package com.example.order.order_service.application.port.out;

import com.example.order.order_service.domain.model.Order;

public interface SaveOrderPort {
    Order save(Order order);
}
