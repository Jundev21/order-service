package com.example.order.order_service.adapter.out.outbox;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxAdapterTest {

    @Mock
    private OutBoxRepository outBoxRepository;

    @InjectMocks
    private OutboxAdapter outboxAdapter;

    @Test
    @DisplayName("Outbox 상태를 SENT로 변경한다")
    void OutboxStatusChangeToSent() {

        Long outboxId = 1L;

        OutboxEventEntity outboxEvent =
                new OutboxEventEntity(
                        "event-123",
                        "ORDER_CREATED",
                        "{\"orderId\":1}"
                );

        given(outBoxRepository.findById(outboxId)).willReturn(Optional.of(outboxEvent));
        outboxAdapter.markAsSent(outboxId);
        assertEquals(OutboxStatus.SENT, outboxEvent.getStatus());
        verify(outBoxRepository).save(outboxEvent);
    }

    @Test
    @DisplayName("PENDING_이벤트를_조회한다")
    void findPendingEvents() {

        OutboxEventEntity event =
                new OutboxEventEntity(
                        "event-123",
                        "ORDER_CREATED",
                        "{}"
                );

        given(outBoxRepository.findAllByStatus(OutboxStatus.PENDING)).willReturn(List.of(event));
        List<OutboxEventEntity> result = outboxAdapter.findPendingEvents();
        assertEquals(1, result.size());
        verify(outBoxRepository).findAllByStatus(OutboxStatus.PENDING);
    }
}