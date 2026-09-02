package com.example.order.order_service.adapter.out.persistence;

import com.example.order.order_service.domain.model.Order;
import com.example.order.order_service.domain.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class OrderPersistenceAdapterTest {

    @Autowired
    private OrderPersistenceAdapter orderPersistenceAdapter;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("주문생성 저장")
    void save() {
        // given
        Order order = Order.create("test-key", 1L, 2);

        // when
        Order savedOrder =
                orderPersistenceAdapter.save(order);

        // then
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getGoodsId()).isEqualTo(1L);
        assertThat(savedOrder.getQuantity()).isEqualTo(2);
        assertThat(savedOrder.getOrderStatus())
                .isEqualTo(OrderStatus.CREATED);
    }
}