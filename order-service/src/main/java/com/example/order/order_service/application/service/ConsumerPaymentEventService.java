package com.example.order.order_service.application.service;

import com.example.order.order_service.application.port.in.ConsumerPaymentEventUseCase;
import com.example.order.order_service.application.port.out.InventoryConsumerEventPort;
import com.example.order.order_service.application.port.out.LoadOrderPort;
import com.example.order.order_service.application.port.out.PaymentOutboxEventPort;
import com.example.order.order_service.application.port.out.PublishOrderEventPort;
import com.example.order.order_service.domain.model.Order;
import com.example.order.order_service.domain.model.OrderStatus;
import com.example.order.order_service.event.PaymentSucceededEvent;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;


@Service
@AllArgsConstructor
public class ConsumerPaymentEventService implements ConsumerPaymentEventUseCase {
    private final InventoryConsumerEventPort inventoryConsumerEventPort;
    private final PublishOrderEventPort publishOrderEventPort;
    private final LoadOrderPort loadOrderPort;
    private final PaymentOutboxEventPort paymentOutboxEventPort;
    private final ObjectMapper objectMapper;


    @Override
    @Transactional
    public void successPayment(PaymentSucceededEvent paymentSucceededEvent) {
        Order orderInfo = loadOrderPort.findById(paymentSucceededEvent.orderId());
        inventoryConsumerEventPort.changeOrderStatus(paymentSucceededEvent.orderId(), OrderStatus.COMPLETED);
    }
}
