package com.example.order.order_service.adapter.in.web;

import com.example.order.order_service.application.port.in.CreateOrderUseCase;
import com.example.order.order_service.domain.model.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
            @NotBlank @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        Order order = createOrderUseCase.createOrder(
                idempotencyKey,
                request.goodsId(),
                request.quantity()
        );

        return ResponseEntity.ok(new CreateOrderResponse(order.getId(), order.getOrderStatus()));
    }
}
