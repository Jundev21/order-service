package com.example.order.order_service.adapter.in.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotNull
        Long goodsId,

        @Min(1)
        int quantity
) {
}
