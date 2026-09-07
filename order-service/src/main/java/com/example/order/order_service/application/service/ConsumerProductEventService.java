package com.example.order.order_service.application.service;

import com.example.order.order_service.application.port.in.ConsumerProductEventUseCase;
import com.example.order.order_service.application.port.out.InventoryConsumerEventPort;
import com.example.order.order_service.application.port.out.PublishOrderEventPort;
import com.example.order.order_service.domain.model.OrderStatus;
import com.example.order.order_service.event.InventoryDecreaseFailedEvent;
import com.example.order.order_service.event.InventoryDecreasedEvent;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class ConsumerProductEventService implements ConsumerProductEventUseCase {

    private final InventoryConsumerEventPort inventoryConsumerEventPort;
    private final PublishOrderEventPort publishOrderEventPort;

    //재고 차감완료 됐을 경우 주문상태가 reserved로 변경되고
    //결제 정보로 이벤트 전송
    // 결제 이벤트 전송하기전에 데이터가 맞는지 outbox 로 묶어서 해결해야한다.

    @Override
    @Transactional
    public void inventoryDecreased(
            InventoryDecreasedEvent event
    ) {
        inventoryConsumerEventPort.changeOrderStatus(
                event.orderId(),
                OrderStatus.RESERVED
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
