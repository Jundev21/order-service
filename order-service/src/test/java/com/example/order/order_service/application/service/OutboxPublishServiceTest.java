package com.example.order.order_service.application.service;

import com.example.order.order_service.adapter.out.outbox.OutboxEventEntity;
import com.example.order.order_service.application.port.out.OutboxEventPort;
import com.example.order.order_service.application.port.out.PublishOrderEventPort;
import com.example.order.order_service.domain.model.OrderStatus;
import com.example.order.order_service.event.OrderCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublishServiceTest {

    @Mock
    private OutboxEventPort outboxEventPort;

    @Mock
    private PublishOrderEventPort publishOrderEventPort;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxPublishService outboxPublishService;

    @Test
    @DisplayName("Kafka 발행에 성공하면 Outbox를 SENT로 변경한다")
    void KafkaSendingSuccess_OutboxChangeToSent() {

        OutboxEventEntity outboxEvent =
                new OutboxEventEntity(
                        "event-123",
                        "ORDER_CREATED",
                        "{\"orderId\":1}"
                );

        OrderCreatedEvent event =
                new OrderCreatedEvent(
                        "event-123",
                        1L,
                        10L,
                        2,
                        OrderStatus.CREATED
                );

        given(outboxEventPort.findPendingEvents()).willReturn(List.of(outboxEvent));
        given(objectMapper.readValue(outboxEvent.getPayload(), OrderCreatedEvent.class)).willReturn(event);

        outboxPublishService.publishPendingEvents();

        verify(publishOrderEventPort, times(1)).publishOrder(event);
        verify(outboxEventPort, times(1)).markAsSent(outboxEvent.getId());
    }

    @Test
    @DisplayName("Kafka 발행에 실패하면 Outbox를 SENT로 변경하지 않는다")
    void FailedKafkaSending_OutboxNotChange() {

        // given
        OutboxEventEntity outboxEvent =
                new OutboxEventEntity(
                        "event-123",
                        "ORDER_CREATED",
                        "{\"orderId\":1}"
                );

        OrderCreatedEvent event =
                new OrderCreatedEvent(
                        "event-123",
                        1L,
                        10L,
                        2,
                        OrderStatus.CREATED

                );

        given(outboxEventPort.findPendingEvents()).willReturn(List.of(outboxEvent));
        given(objectMapper.readValue(outboxEvent.getPayload(), OrderCreatedEvent.class)).willReturn(event);
        willThrow(new IllegalStateException("Kafka 발행 실패")).given(publishOrderEventPort).publishOrder(event);
        assertThrows(IllegalStateException.class, () -> outboxPublishService.publishPendingEvents());

        verify(outboxEventPort, never()).markAsSent(anyLong());
    }
}