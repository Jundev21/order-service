package com.example.order.order_service.adapter.in.web;

import com.example.order.order_service.application.port.in.CreateOrderUseCase;
import com.example.order.order_service.domain.model.Order;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
    }

    @PostMapping("/order")
    public ResponseEntity<CreateOrderResponse> createNewOrder(
            @Valid @RequestBody CreateOrderRequest createOrderRequest
    ) {
        Order order = createOrderUseCase.createOrder(
                createOrderRequest.goodsId(),
                createOrderRequest.quantity()
        );

        CreateOrderResponse response = new CreateOrderResponse(
                order.getId(),
                order.getOrderStatus()
        );

        return ResponseEntity.ok(response);
    }
}
