package com.example.order.order_service.application.service;

import com.example.order.order_service.application.port.in.ConsumerProductEventUseCase;
import com.example.order.order_service.application.port.out.InventoryConsumerEventPort;
import com.example.order.order_service.application.port.out.LoadOrderPort;
import com.example.order.order_service.application.port.out.OutboxEventPort;
import com.example.order.order_service.application.port.out.PublishOrderEventPort;
import com.example.order.order_service.domain.model.Order;
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
    private final LoadOrderPort loadOrderPort;
    private final OutboxEventPort outboxEventPort;

    @Override
    @Transactional
    public void inventoryDecreased(
            InventoryDecreasedEvent event
    ) {

        //상품 서비스에서 차감 완료 메세지가오면 상태 reserved 로 변경
        Order orderInfo = loadOrderPort.findById(event.orderId());

        inventoryConsumerEventPort.changeOrderStatus(event.orderId(), OrderStatus.RESERVED);
        //차감이 완료됐으니 결제 서비스에 이벤트 전송해야됨
        //결제 이벤트 전송하기전에 데이터가 맞는지 outbox 로 묶어서 해결해야한다.







//        outboxEventPort.save();






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
