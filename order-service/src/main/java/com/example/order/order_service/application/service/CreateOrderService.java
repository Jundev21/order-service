package com.example.order.order_service.application.service;

import com.example.order.order_service.application.port.in.CreateOrderUseCase;
import com.example.order.order_service.application.port.out.LoadOrderPort;
import com.example.order.order_service.application.port.out.PublishOrderEventPort;
import com.example.order.order_service.application.port.out.SaveOrderPort;
import com.example.order.order_service.domain.model.Order;
import com.example.order.order_service.event.OrderCreatedEvent;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CreateOrderService implements CreateOrderUseCase {
    private final SaveOrderPort saveOrderPort;
    private final PublishOrderEventPort publishOrderEventPort;
    private final LoadOrderPort loadOrderPort;

    @Override
    public Order createOrder(String idempotencyKey, Long goodsId, int quantity) {

        Optional<Order> existingOrder = loadOrderPort.findByIdempotencyKey(idempotencyKey);

        if(existingOrder.isPresent()) return existingOrder.get();

        Order order = Order.create(idempotencyKey, goodsId, quantity);
        Order savedOrder = saveOrderPort.save(order);
        String eventId = UUID.randomUUID().toString();


        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.from(savedOrder, eventId);
        publishOrderEventPort.publishOrder(orderCreatedEvent);

        return savedOrder;
    }
}