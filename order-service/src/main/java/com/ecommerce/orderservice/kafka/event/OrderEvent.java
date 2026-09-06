package com.ecommerce.orderservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Published to "order-events" on order creation, cancellation, and status
 * changes. inventory-service reacts to ORDER_CREATED/ORDER_CANCELLED;
 * payment-service reacts to ORDER_CREATED; notification-service reacts
 * to all of them.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;

    private String orderId;
    private String userId;
    private BigDecimal amount;
    private List<OrderItemPayload> items;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemPayload {
        private String productId;
        private int quantity;
    }
}
