package com.example.order.order_service.application.service;

import com.example.order.order_service.application.port.in.CreateOrderUseCase;
import com.example.order.order_service.application.port.out.SaveOrderPort;
import com.example.order.order_service.domain.model.Order;
import org.springframework.stereotype.Service;

@Service
public class CreateOrderService implements CreateOrderUseCase {
    private final SaveOrderPort saveOrderPort;

    public CreateOrderService(SaveOrderPort saveOrderPort) {
        this.saveOrderPort = saveOrderPort;
    }

    @Override
    public Order createOrder(Long goodsId, int quantity) {
        Order order = Order.create(goodsId, quantity);
        return saveOrderPort.save(order);
    }
}