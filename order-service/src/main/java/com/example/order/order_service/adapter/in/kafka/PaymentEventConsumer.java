package com.example.order.order_service.adapter.in.kafka;

import com.example.order.order_service.application.port.in.ConsumerPaymentEventUseCase;
import com.example.order.order_service.event.PaymentFailedEvent;
import com.example.order.order_service.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final ConsumerPaymentEventUseCase consumerPaymentEventUseCase;
    private static final String PAYMENT_TOPIC = "success-payment";
    private static final String PAYMENT_FAILED_TOPIC = "failed-payment";
    private static final String GROUP_ID = "order-service";


    @KafkaListener(
            topics = PAYMENT_TOPIC,
            groupId = GROUP_ID
    )
    public void successPayment(
            PaymentSucceededEvent paymentSucceededEvent
    ) {
        consumerPaymentEventUseCase.successPayment(paymentSucceededEvent);
    }

    @KafkaListener(
            topics = PAYMENT_FAILED_TOPIC,
            groupId = GROUP_ID
    )
    public void failedPayment(
            PaymentFailedEvent paymentFailedEvent
    ) {
        consumerPaymentEventUseCase.failedPayment(paymentFailedEvent);
    }
}
