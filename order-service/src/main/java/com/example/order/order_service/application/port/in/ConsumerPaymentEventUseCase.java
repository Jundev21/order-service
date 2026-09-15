package com.example.order.order_service.application.port.in;

import com.example.order.order_service.event.InventoryDecreaseFailedEvent;
import com.example.order.order_service.event.InventoryDecreasedEvent;
import com.example.order.order_service.event.PaymentSucceededEvent;

public interface ConsumerPaymentEventUseCase {
    void successPayment(PaymentSucceededEvent paymentSucceededEvent);
}
