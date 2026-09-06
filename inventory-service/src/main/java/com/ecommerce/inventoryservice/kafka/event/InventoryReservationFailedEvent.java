package com.ecommerce.inventoryservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Published when a Kafka-triggered reservation (from an ORDER_CREATED
 * event) fails due to insufficient stock. order-service consumes this to
 * transition the order to CANCELLED instead of leaving it stuck PENDING.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservationFailedEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;

    private String orderId;
    private String reason;
}
