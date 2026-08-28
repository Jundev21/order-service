package com.example.order.order_service.adapter.in.kafka;

import com.example.order.order_service.application.port.in.ConsumerProductEventPort;
import com.example.order.order_service.event.InventoryDecreaseFailedEvent;
import com.example.order.order_service.event.InventoryDecreasedEvent;
import com.example.order.order_service.event.OrderCreatedEvent;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ProductEventConsumer {

    private final ConsumerProductEventPort consumerProductEventPort;

    private static final String DECREASED_TOPIC = "inventory-decreased";
    private static final String DECREASE_FAILED_TOPIC = "inventory-decrease-failed";
    private final String GROUPID = "order-service";

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;


    @KafkaListener(
            topics = DECREASED_TOPIC,
            groupId = GROUPID
    )
    public void inventoryDecreased(InventoryDecreasedEvent inventoryDecreasedEvent) {

        consumerProductEventPort.inventoryDecreased(inventoryDecreasedEvent);

    }


    @KafkaListener(
            topics = DECREASE_FAILED_TOPIC,
            groupId = GROUPID
    )
    public void inventoryFailedDecreased(InventoryDecreaseFailedEvent inventoryDecreaseFailedEvent) {

        consumerProductEventPort.inventoryFailedDecreased(inventoryDecreaseFailedEvent);

    }

}
