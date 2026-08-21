package com.example.order.order_service.adapter.out.persistence;

import com.example.order.order_service.application.port.out.SaveOrderPort;
import com.example.order.order_service.domain.model.Order;
import org.springframework.stereotype.Component;

// entity 하고 application out 을 연동하는부분 실질적으로 out 의 구현체
@Component
public class OrderPersistenceAdapter implements SaveOrderPort {
    private final OrderRepository orderRepository;

    public OrderPersistenceAdapter(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order save(Order order) {

        OrderEntity orderEntity = new OrderEntity(
                order.getId(),
                order.getGoodsId(),
                order.getQuantity(),
                order.getOrderStatus()
        );

        OrderEntity saveOrderEntity = orderRepository.save(orderEntity);

        return new Order(
                saveOrderEntity.getId(),
                saveOrderEntity.getGoodsId(),
                saveOrderEntity.getQuantity(),
                saveOrderEntity.getOrderStatus()
        );
    }
}
