package com.example.order.order_service.domain.model;

import lombok.Getter;

// 순수 Order 에 관한 로직

@Getter
public class Order {

    private final Long id;
    private final Long goodsId;
    private final int quantity;
    private OrderStatus orderStatus;


    public Order(Long id, Long goodsId, int quantity, OrderStatus orderStatus) {
        this.id = id;
        this.goodsId = goodsId;
        this.quantity = quantity;
        this.orderStatus = orderStatus;
    }


    public static Order create(Long goodsId, int quantity){
        return new Order(
                null,
                goodsId,
                quantity,
                OrderStatus.CREATED
        );

    }

    public void cancle(){
        this.orderStatus = OrderStatus.CANCELED;
    }

    public void complete(Long goodsId, int quantity){
        this.orderStatus = OrderStatus.COMPLETED;
    }

}
