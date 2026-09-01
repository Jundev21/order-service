package com.example.order.order_service.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity,Long> {
    Optional<OrderEntity> findByIdempotencyKey(String idempotencyKey);
}
