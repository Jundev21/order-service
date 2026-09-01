package com.example.order.order_service.adapter.out.persistence;

import com.example.order.order_service.application.port.out.LoadOrderPort;
import com.example.order.order_service.application.port.out.SaveOrderPort;
import com.example.order.order_service.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

// entity 하고 application out 을 연동하는부분 실질적으로 out 의 구현체
@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements SaveOrderPort, LoadOrderPort {
    private final OrderRepository orderRepository;

    @Override
    public Order save(Order order) {

        OrderEntity orderEntity = new OrderEntity(
                order.getId(),
                order.getIdempotencyKey(),
                order.getGoodsId(),
                order.getQuantity(),
                order.getOrderStatus()
        );

        OrderEntity saveOrderEntity = orderRepository.save(orderEntity);

        return new Order(
                saveOrderEntity.getId(),
                order.getIdempotencyKey(),
                saveOrderEntity.getGoodsId(),
                saveOrderEntity.getQuantity(),
                saveOrderEntity.getOrderStatus()
        );
    }

    @Override
    public Optional<Order> findByIdempotencyKey(String idempotencyKey) {
        return orderRepository
                .findByIdempotencyKey(idempotencyKey)
                .map(orderEntity ->
                        new Order(
                                orderEntity.getId(),
                                orderEntity.getIdempotencyKey(),
                                orderEntity.getGoodsId(),
                                orderEntity.getQuantity(),
                                orderEntity.getOrderStatus()
                        )
                );
    }
}
