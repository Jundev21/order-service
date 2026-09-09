package com.example.order.order_service.domain.model;

import lombok.Getter;

// 순수 Order 에 관한 로직

@Getter
public class Order {

    private final Long id;
    private final String idempotencyKey;
    private final Long goodsId;
    private final Long productPrice;
    private final int quantity;
    private OrderStatus orderStatus;
    private final Long totalPrice;


    public Order(Long id, String idempotencyKey, Long goodsId, Long productPrice, int quantity, OrderStatus orderStatus) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.goodsId = goodsId;
        this.productPrice = productPrice;
        this.quantity = quantity;
        this.orderStatus = orderStatus;
        this.totalPrice = quantity * productPrice;
    }


    public static Order create(String idempotencyKey, Long goodsId, int quantity, Long productPrice) {
        return new Order(
                null,
                idempotencyKey,
                goodsId,
                productPrice,
                quantity,
                OrderStatus.CREATED
        );

    }

    public void cancle() {
        this.orderStatus = OrderStatus.CANCELED;
    }

    public void complete(Long goodsId, int quantity) {
        this.orderStatus = OrderStatus.COMPLETED;
    }

}
