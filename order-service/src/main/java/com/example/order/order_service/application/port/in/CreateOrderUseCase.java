package com.example.order.order_service.application.port.in;

import com.example.order.order_service.domain.model.Order;

//단일책임 = create order 에 관련된 로직만 들어감.
//흐름 제어

public interface CreateOrderUseCase {
    Order createOrder(String idempotencyKey, Long goodsId, int quantity);
}
