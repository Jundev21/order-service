package com.example.order.order_service.adapter.in.web;

import com.example.order.order_service.application.port.in.CreateOrderUseCase;
import com.example.order.order_service.domain.model.Order;
import com.example.order.order_service.domain.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class OrderControllerTest {

    private MockMvc mockMvc;

    private CreateOrderUseCase createOrderUseCase;

    @BeforeEach
    void setUp() {

        createOrderUseCase =
                Mockito.mock(CreateOrderUseCase.class);

        OrderController orderController =
                new OrderController(createOrderUseCase);

        mockMvc = MockMvcBuilders
                .standaloneSetup(orderController)
                .build();
    }

    @Test
    void orderController() throws Exception {

        Order order = new Order(
                1L,
                1L,
                2,
                OrderStatus.CREATED
        );

        when(
                createOrderUseCase.createOrder(1L, 2)
        ).thenReturn(order);

        mockMvc.perform(
                        post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "goodsId": 1,
                                          "quantity": 2
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1));



        verify(createOrderUseCase)
                .createOrder(1L, 2);
    }
}