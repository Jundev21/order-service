package com.example.order.order_service.adapter.out.outbox.product;

import com.example.order.order_service.adapter.out.outbox.OutboxStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "outbox_events",
        indexes = {
                @Index(
                        name = "idx_outbox_status_id",
                        columnList = "status, id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOutboxEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;

    private String eventType;

    @Lob
    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    public ProductOutboxEventEntity(String eventId, String eventType, String payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
    }

    public void updateStatus(OutboxStatus status) {
        this.status = status;
    }

    public void markAsSent() {
        status = OutboxStatus.SENT;
    }

}