package com.example.order.order_service.application.service;

import com.example.order.order_service.application.port.in.ConsumerProductEventPort;
import com.example.order.order_service.event.InventoryDecreaseFailedEvent;
import com.example.order.order_service.event.InventoryDecreasedEvent;
import org.springframework.stereotype.Service;


@Service
public class ConsumerProductEventService implements ConsumerProductEventPort {
    @Override
    public void inventoryDecreased(InventoryDecreasedEvent inventoryDecreasedEvent) {

    }

    @Override
    public void inventoryFailedDecreased(InventoryDecreaseFailedEvent inventoryDecreaseFailedEvent) {

    }
}
