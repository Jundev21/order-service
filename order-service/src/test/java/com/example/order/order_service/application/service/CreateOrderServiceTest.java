package com.example.order.order_service.application.service;

import com.example.order.order_service.application.port.out.LoadOrderPort;
import com.example.order.order_service.application.port.out.OutboxEventPort;
import com.example.order.order_service.application.port.out.SaveOrderPort;
import com.example.order.order_service.domain.model.Order;
import com.example.order.order_service.domain.model.OrderStatus;
import com.example.order.order_service.event.OrderCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

    @Mock
    private SaveOrderPort saveOrderPort;

    @Mock
    private LoadOrderPort loadOrderPort;

    @Mock
    private OutboxEventPort outboxEventPort;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CreateOrderService createOrderService;

    @Test
    @DisplayName("주문을_생성하면_Outbox_이벤트도_저장된다")
    void OrderOutBoxEventSave() {

        String idempotencyKey = "test-key";
        Long goodsId = 1L;
        int quantity = 2;

        Order savedOrder = new Order(
                1L,
                idempotencyKey,
                goodsId,
                quantity,
                OrderStatus.CREATED
        );

        given(loadOrderPort.findByIdempotencyKey(idempotencyKey)).willReturn(Optional.empty());
        given(saveOrderPort.save(any(Order.class))).willReturn(savedOrder);
        given(objectMapper.writeValueAsString(any(OrderCreatedEvent.class))).willReturn("{\"orderId\":1}");

        Order result = createOrderService.createOrder(
                idempotencyKey,
                goodsId,
                quantity
        );

        verify(saveOrderPort, times(1)).save(any(Order.class));
        verify(outboxEventPort, times(1))
                .save(
                        anyString(),
                        eq("ORDER_CREATED"),
                        eq("{\"orderId\":1}")
                );

        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("동일한 멱등키의 주문이 이미 있으면 Outbox를 생성하지 않는다")
    void DuplicateOrder_OutboxEventNotSave() {

        String idempotencyKey = "same-key";

        Order existingOrder = new Order(
                1L,
                idempotencyKey,
                1L,
                2,
                OrderStatus.CREATED
        );

        given(loadOrderPort.findByIdempotencyKey(idempotencyKey))
                .willReturn(Optional.of(existingOrder));

        Order result =
                createOrderService.createOrder(
                        idempotencyKey,
                        1L,
                        2
                );

        verify(saveOrderPort, never())
                .save(any());

        verify(outboxEventPort, never())
                .save(anyString(), anyString(), anyString());

        assertEquals(1L, result.getId());
    }
}