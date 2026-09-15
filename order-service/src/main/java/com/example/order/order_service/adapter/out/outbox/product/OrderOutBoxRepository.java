package com.example.order.order_service.adapter.out.outbox.product;

import com.example.order.order_service.adapter.out.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderOutBoxRepository extends JpaRepository<OrderOutboxEventEntity,Long> {
    //만약 pending 이 1000만건처럼 대량으로 있다고 가정하면 한번에 다 읽기보다는 100개씩 끊어서 조회
    List<OrderOutboxEventEntity> findTop100ByStatusOrderByIdAsc(OutboxStatus status );
}