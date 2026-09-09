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

    private String idempotencyKey;

    private Long goodsId;

    private Long unitPrice;

    private int quantity;

    private Long totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    public OrderEntity(Long id, String idempotencyKey, Long goodsId, int quantity, OrderStatus orderStatus,Long unitPrice) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.goodsId = goodsId;
        this.quantity = quantity;
        this.orderStatus = orderStatus;
        this.totalAmount = quantity * unitPrice;
        this.unitPrice = unitPrice;
    }

    public void ChangeOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
