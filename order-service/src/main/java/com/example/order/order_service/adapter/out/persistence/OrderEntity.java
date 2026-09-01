package com.example.order.order_service.adapter.out.persistence;

import com.example.order.order_service.domain.model.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long goodsId;

    @Column(unique = true)
    private String idempotencyKey;

    private int quantity;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    public OrderEntity(Long id, String idempotencyKey, Long goodsId, int quantity, OrderStatus orderStatus) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.goodsId = goodsId;
        this.quantity = quantity;
        this.orderStatus = orderStatus;
    }

    public void ChangeOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
