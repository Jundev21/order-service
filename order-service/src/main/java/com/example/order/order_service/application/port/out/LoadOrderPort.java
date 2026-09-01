package com.example.order.order_service.application.port.out;

import com.example.order.order_service.domain.model.Order;

import java.util.Optional;

public interface LoadOrderPort {
    Optional<Order> findByIdempotencyKey(String idempotencyKey);
}