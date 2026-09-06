package com.ecommerce.orderservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Mirrors the event shape published by payment-service to "payment-events".
 * order-service consumes PAYMENT_SUCCESS (-> CONFIRMED, permanently deduct
 * stock) and PAYMENT_FAILED (-> CANCELLED, release reserved stock).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;

    private String orderId;
    private String paymentId;
    private String reason;
}
