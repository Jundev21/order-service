package com.example.order.order_service.adapter.out.outbox;

import com.example.order.order_service.application.port.out.OutboxEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

// entity 하고 application out 을 연동하는부분 실질적으로 out 의 구현체
@Component
@RequiredArgsConstructor
public class OutboxAdapter implements OutboxEventPort {
    private final OutBoxRepository outBoxRepository;

    @Override
    public void save(String eventId, String eventType, String payload) {
        OutboxEventEntity outboxEventEntity = new OutboxEventEntity(eventId, eventType, payload);
        outBoxRepository.save(outboxEventEntity);
    }

    @Override
    public List<OutboxEventEntity> findPendingEvents() {
        return outBoxRepository.findTop100ByStatusOrderByIdAsc(OutboxStatus.PENDING);
    }

    @Override
    public void markAsSent(Long id) {
        OutboxEventEntity outboxEventEntity = outBoxRepository.findById(id).orElseThrow(() ->
                new IllegalStateException(
                        "Outbox 이벤트를 찾을 수 없습니다."
                )
        );
        outboxEventEntity.updateStatus(OutboxStatus.SENT);
        outBoxRepository.save(outboxEventEntity);

    }
}
