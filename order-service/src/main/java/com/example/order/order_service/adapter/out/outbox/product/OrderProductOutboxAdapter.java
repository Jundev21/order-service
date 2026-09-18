package com.example.order.order_service.adapter.out.outbox.product;

import com.example.order.order_service.adapter.out.outbox.OutboxStatus;
import com.example.order.order_service.application.port.out.ProductOutboxEventPort;
import com.example.order.order_service.application.port.out.dto.PendingOutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

// entity 하고 application out 을 연동하는부분 실질적으로 out 의 구현체
@Component
@RequiredArgsConstructor
public class OrderProductOutboxAdapter implements ProductOutboxEventPort {
    private final ProductOutBoxRepository productOutboxRepository;

    @Override
    public void save(String eventId, String eventType, String payload) {
        productOutboxRepository.save(new ProductOutboxEventEntity(eventId, eventType, payload));
    }

    @Override
    public List<PendingOutboxEvent> findPendingEvents(String eventType) {
        return productOutboxRepository.findTop100ByStatusAndEventTypeOrderByIdAsc(OutboxStatus.PENDING, eventType)
                .stream()
                .map(event -> new PendingOutboxEvent(event.getId(), event.getPayload()))
                .toList();
    }

    @Override
    public void markAsSent(Long id) {
        ProductOutboxEventEntity event = productOutboxRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("주문 Outbox 이벤트를 찾을 수 없습니다. id=" + id));
        event.markAsSent();
        productOutboxRepository.save(event);
    }
}
