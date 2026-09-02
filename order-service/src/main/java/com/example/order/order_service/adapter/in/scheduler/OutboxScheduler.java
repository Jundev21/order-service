package com.example.order.order_service.adapter.in.scheduler;

import com.example.order.order_service.application.service.OutboxPublishService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxPublishService outboxPublishService;

    @Scheduled(fixedDelay = 1000)
    public void publishOutboxEvents() {
        outboxPublishService.publishPendingEvents();
    }
}