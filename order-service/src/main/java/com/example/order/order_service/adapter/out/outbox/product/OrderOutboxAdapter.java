package com.example.order.order_service.adapter.out.outbox.product;

import com.example.order.order_service.adapter.out.outbox.OutboxStatus;
import com.example.order.order_service.application.port.out.OutboxEventPort;
import com.example.order.order_service.application.port.out.dto.PendingOutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

// entity 하고 application out 을 연동하는부분 실질적으로 out 의 구현체
@Component
@RequiredArgsConstructor
public class OrderOutboxAdapter implements OutboxEventPort {
    private final OrderOutBoxRepository orderOutboxRepository;

    @Override
    public void save(String eventId, String eventType, String payload) {
        orderOutboxRepository.save(new OrderOutboxEventEntity(eventId, eventType, payload));
    }

    @Override
    public List<PendingOutboxEvent> findPendingEvents() {
        return orderOutboxRepository.findTop100ByStatusOrderByIdAsc(OutboxStatus.PENDING)
                .stream()
                .map(event -> new PendingOutboxEvent(event.getId(), event.getPayload()))
                .toList();
    }

    @Override
    public void markAsSent(Long id) {
        OrderOutboxEventEntity event = orderOutboxRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("주문 Outbox 이벤트를 찾을 수 없습니다. id=" + id));
        event.markAsSent();
        orderOutboxRepository.save(event);
    }
}
