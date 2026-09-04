package com.example.order.order_service.adapter.in.kafka;

import com.example.order.order_service.application.port.in.ConsumerProductEventUseCase;
import com.example.order.order_service.event.InventoryDecreaseFailedEvent;
import com.example.order.order_service.event.InventoryDecreasedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private final ConsumerProductEventUseCase consumerProductEventUseCase;
    private static final String DECREASED_TOPIC = "inventory-decreased";
    private static final String DECREASE_FAILED_TOPIC = "inventory-decrease-failed";
    private static final String GROUP_ID = "order-service";


    @KafkaListener(
            topics = DECREASED_TOPIC,
            groupId = GROUP_ID
    )
    public void inventoryDecreased(
            InventoryDecreasedEvent event
    ) {
        consumerProductEventUseCase.inventoryDecreased(event);
    }


    @KafkaListener(
            topics = DECREASE_FAILED_TOPIC,
            groupId = GROUP_ID
    )
    public void inventoryDecreaseFailed(
            InventoryDecreaseFailedEvent event
    ) {
        consumerProductEventUseCase.inventoryFailedDecreased(event);
    }
}
