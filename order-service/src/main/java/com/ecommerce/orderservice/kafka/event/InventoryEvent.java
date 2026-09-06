package com.ecommerce.orderservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Mirrors the event shape published by inventory-service to
 * "inventory-events". order-service only reacts to
 * INVENTORY_RESERVATION_FAILED, as a safety net for the asynchronous
 * reservation path (order-service's synchronous Feign reserve call
 * already handles the common case at checkout time).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;

    private String orderId;
    private String reason;
}
