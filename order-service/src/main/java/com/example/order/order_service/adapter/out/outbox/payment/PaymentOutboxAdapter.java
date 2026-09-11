package com.example.order.order_service.adapter.out.outbox.payment;

import com.example.order.order_service.adapter.out.outbox.OutboxStatus;
import com.example.order.order_service.adapter.out.outbox.product.OutBoxRepository;
import com.example.order.order_service.adapter.out.outbox.product.OutboxEventEntity;
import com.example.order.order_service.application.port.out.OutboxEventPort;
import com.example.order.order_service.application.port.out.PaymentOutboxEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

// entity 하고 application out 을 연동하는부분 실질적으로 out 의 구현체
@Component
@RequiredArgsConstructor
public class PaymentOutboxAdapter implements PaymentOutboxEventPort {
    private final PaymentOutBoxRepository paymentOutBoxRepository;

    @Override
    public void save(String eventId, String eventType, String payload) {
        PaymentOutboxEventEntity paymentOutboxEventEntity = new PaymentOutboxEventEntity(eventId, eventType, payload);
        paymentOutBoxRepository.save(paymentOutboxEventEntity);
    }

    @Override
    public List<PaymentOutboxEventEntity> findPendingEvents() {
        return paymentOutBoxRepository.findTop100ByStatusOrderByIdAsc(OutboxStatus.PENDING);
    }

    @Override
    public void markAsSent(Long id) {
        PaymentOutboxEventEntity paymentOutboxEventEntity = paymentOutBoxRepository.findById(id).orElseThrow(() ->
                new IllegalStateException(
                        "Outbox 이벤트를 찾을 수 없습니다."
                )
        );
        paymentOutboxEventEntity.updateStatus(OutboxStatus.SENT);
        paymentOutBoxRepository.save(paymentOutboxEventEntity);

    }
}
