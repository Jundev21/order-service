package com.example.order.order_service.adapter.out.persistence;

import com.example.order.order_service.application.port.out.InventoryConsumerEventPort;
import com.example.order.order_service.domain.model.Order;
import com.example.order.order_service.domain.model.OrderStatus;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class InventoryConsumerAdapter implements InventoryConsumerEventPort {

    private final OrderRepository orderRepository;

    @Override
    public void changeOrderStatus(Long orderId, OrderStatus orderStatus) {
        OrderEntity orderEntity = orderRepository.findById(orderId).orElseThrow();
        orderEntity.ChangeOrderStatus(orderStatus);
        orderRepository.save(orderEntity);
    }

}
