package com.example.order.order_service.event;

import com.example.order.order_service.domain.model.Order;

public record RequestPaymentEvent(
        String eventId,
        Long orderId,
        Long amount
) {
    public static RequestPaymentEvent from(
            Order order, String eventId, Long amount
    ) {
        return new RequestPaymentEvent(
                eventId,
                order.getId(),
                amount
        );
    }
}
