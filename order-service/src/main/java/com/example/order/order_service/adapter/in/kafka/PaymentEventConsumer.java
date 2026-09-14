package com.example.order.order_service.adapter.in.kafka;

import com.example.order.order_service.application.port.in.ConsumerProductEventUseCase;
import com.example.order.order_service.event.InventoryDecreaseFailedEvent;
import com.example.order.order_service.event.InventoryDecreasedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final ConsumerProductEventUseCase consumerProductEventUseCase;
    private static final String PAYMENT_TOPIC = "success-payment";
    private static final String GROUP_ID = "order-service";


    @KafkaListener(
            topics = PAYMENT_TOPIC,
            groupId = GROUP_ID
    )
    public void successPayment(
            InventoryDecreasedEvent event
    ) {
    }
}
