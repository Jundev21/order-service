package com.example.order.order_service.application.service;

import com.example.order.order_service.application.port.in.CreateOrderUseCase;
import com.example.order.order_service.application.port.out.PublishOrderEventPort;
import com.example.order.order_service.application.port.out.SaveOrderPort;
import com.example.order.order_service.domain.model.Order;
import com.example.order.order_service.event.OrderCreatedEvent;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CreateOrderService implements CreateOrderUseCase {
    private final SaveOrderPort saveOrderPort;
    private final PublishOrderEventPort publishOrderEventPort;

    @Override
    public Order createOrder(Long goodsId, int quantity) {
        Order order = Order.create(goodsId, quantity);
        Order savedOrder = saveOrderPort.save(order);

        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.from(savedOrder);
        publishOrderEventPort.publishOrder(orderCreatedEvent);

        return savedOrder;
    }
}