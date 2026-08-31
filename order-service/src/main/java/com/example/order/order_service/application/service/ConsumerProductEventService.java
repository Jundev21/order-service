package com.example.order.order_service.application.service;

import com.example.order.order_service.application.port.in.ConsumerProductEventPort;
import com.example.order.order_service.application.port.out.InventoryConsumerEventPort;
import com.example.order.order_service.domain.model.OrderStatus;
import com.example.order.order_service.event.InventoryDecreaseFailedEvent;
import com.example.order.order_service.event.InventoryDecreasedEvent;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class ConsumerProductEventService implements ConsumerProductEventPort {

    private final InventoryConsumerEventPort inventoryConsumerEventPort;

    @Override
    @Transactional
    public void inventoryDecreased(
            InventoryDecreasedEvent event
    ) {

        inventoryConsumerEventPort.changeOrderStatus(
                event.orderId(),
                OrderStatus.COMPLETED
        );
    }

    @Override
    @Transactional
    public void inventoryFailedDecreased(
            InventoryDecreaseFailedEvent event
    ) {

        inventoryConsumerEventPort.changeOrderStatus(
                event.orderId(),
                OrderStatus.FAILED
        );
    }
}
