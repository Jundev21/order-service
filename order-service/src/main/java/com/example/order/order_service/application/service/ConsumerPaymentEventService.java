package com.example.order.order_service.application.service;

import com.example.order.order_service.application.port.in.ConsumerPaymentEventUseCase;
import com.example.order.order_service.application.port.out.*;
import com.example.order.order_service.domain.model.Order;
import com.example.order.order_service.domain.model.OrderStatus;
import com.example.order.order_service.event.PaymentFailedEvent;
import com.example.order.order_service.event.PaymentSucceededEvent;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;


@Service
@AllArgsConstructor
public class ConsumerPaymentEventService implements ConsumerPaymentEventUseCase {
    private final InventoryConsumerEventPort inventoryConsumerEventPort;
    private final PublishOrderEventPort publishOrderEventPort;
    private final LoadOrderPort loadOrderPort;
    private final PaymentOutboxEventPort paymentOutboxEventPort;
    private final ProductOutboxEventPort productOutboxEventPort;
    private final ObjectMapper objectMapper;


    @Override
    @Transactional
    public void successPayment(PaymentSucceededEvent paymentSucceededEvent) {
        Order orderInfo = loadOrderPort.findById(paymentSucceededEvent.orderId());
        inventoryConsumerEventPort.changeOrderStatus(paymentSucceededEvent.orderId(), OrderStatus.COMPLETED);
    }


    //결제 실패시 상품으로 재고 증가 요청보내야함 왜냐하면 결제전에 이미 재고 차감이 완료된상태니까
    @Override
    @Transactional
    public void failedPayment(PaymentFailedEvent paymentFailedEvent) {
        System.out.println("상품실패 이벤트");
        Order orderInfo = loadOrderPort.findById(paymentFailedEvent.orderId());
        inventoryConsumerEventPort.changeOrderStatus(paymentFailedEvent.orderId(), OrderStatus.FAILED);
        String eventId = UUID.randomUUID().toString();
        String payload = objectMapper.writeValueAsString(paymentFailedEvent);
        inventoryConsumerEventPort.changeOrderStatus(
                paymentFailedEvent.orderId(),
                OrderStatus.FAILED
        );
        productOutboxEventPort.save(eventId, "INCREASE_INVENTORY", payload);
    }
}
