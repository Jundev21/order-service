package com.example.order.order_service.application.service;

import com.example.order.order_service.application.port.out.SaveOrderPort;
import com.example.order.order_service.domain.model.Order;
import com.example.order.order_service.domain.model.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

    @Mock
    private SaveOrderPort saveOrderPort;

    @InjectMocks
    private CreateOrderService createOrderService;

    @Test
    @DisplayName("주문생성")
    void createOrder() {
        // given
        Long goodsId = 1L;
        int quantity = 2;

        Order savedOrder = new Order(
                1L,
                goodsId,
                quantity,
                OrderStatus.CREATED
        );

        when(saveOrderPort.save(any(Order.class)))
                .thenReturn(savedOrder);

        // when
        Order result =
                createOrderService.createOrder(goodsId, quantity);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getGoodsId()).isEqualTo(1L);
        assertThat(result.getQuantity()).isEqualTo(2);
        assertThat(result.getOrderStatus())
                .isEqualTo(OrderStatus.CREATED);
    }
}