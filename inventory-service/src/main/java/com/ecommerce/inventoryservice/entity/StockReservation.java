package com.ecommerce.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One row per (orderId, productId) reservation. Its existence is the
 * source of truth for "has this order's stock already been reserved" —
 * used to make POST /api/inventory/reserve and the Kafka-triggered
 * reservation path idempotent against retries/redelivery, and to know
 * exactly what to restore on release (rather than trusting whatever
 * item list a release request happens to carry).
 */
@Entity
@Table(name = "stock_reservations", indexes = {
        @Index(name = "idx_reservation_order", columnList = "order_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReservation {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(nullable = false)
    private int quantity;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
