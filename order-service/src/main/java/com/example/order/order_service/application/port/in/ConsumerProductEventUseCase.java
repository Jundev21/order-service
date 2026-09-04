package com.example.order.order_service.application.port.in;

import com.example.order.order_service.event.InventoryDecreaseFailedEvent;
import com.example.order.order_service.event.InventoryDecreasedEvent;

public interface ConsumerProductEventUseCase {
    void inventoryDecreased(InventoryDecreasedEvent inventoryDecreasedEvent);
    void inventoryFailedDecreased(InventoryDecreaseFailedEvent inventoryDecreaseFailedEvent);
}
