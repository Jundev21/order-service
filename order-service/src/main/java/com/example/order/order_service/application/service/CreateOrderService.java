package com.example.order.order_service.application.service;

import com.example.order.order_service.application.port.in.CreateOrderUseCase;
import com.example.order.order_service.application.port.out.LoadOrderPort;
import com.example.order.order_service.application.port.out.LoadProductPort;
import com.example.order.order_service.application.port.out.ProductOutboxEventPort;
import com.example.order.order_service.application.port.out.SaveOrderPort;
import com.example.order.order_service.application.port.out.dto.ProductInfo;
import com.example.order.order_service.domain.model.Order;
import com.example.order.order_service.event.OrderCreatedEvent;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CreateOrderService implements CreateOrderUseCase {
    private final SaveOrderPort saveOrderPort;
    private final LoadOrderPort loadOrderPort;
    private final LoadProductPort loadProductPort;
    private final ProductOutboxEventPort productOutboxEventPort;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Order createOrder(String idempotencyKey, Long goodsId, int quantity) {

        Optional<Order> existingOrder = loadOrderPort.findByIdempotencyKey(idempotencyKey);

        if (existingOrder.isPresent()) return existingOrder.get();

        ProductInfo product = loadProductPort.getProductInfo(goodsId);

        Long totalPrice = product.price() * quantity;

        Order order = Order.create(idempotencyKey, goodsId, quantity, product.price());
        Order savedOrder = saveOrderPort.save(order);
        String eventId = UUID.randomUUID().toString();

        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.from(savedOrder, eventId);
        String payload = objectMapper.writeValueAsString(orderCreatedEvent);
        productOutboxEventPort.save(eventId, "ORDER_CREATED", payload);
        return savedOrder;
    }
}