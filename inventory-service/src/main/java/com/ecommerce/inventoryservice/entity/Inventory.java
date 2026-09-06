package com.ecommerce.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Tracks stock for a single productId. availableQuantity is what can
 * still be sold; reservedQuantity is stock provisionally held for
 * in-flight orders (reserved at checkout, released on payment failure
 * or order cancellation, permanently deducted on payment success).
 *
 * @Version enables optimistic locking so two concurrent reservation
 * requests for the last unit of stock can't both succeed — the second
 * writer gets an OptimisticLockException and the service layer retries
 * or fails cleanly instead of silently overselling.
 */
@Entity
@Table(name = "inventory")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @Column(name = "product_id")
    private String productId;

    @Column(nullable = false)
    @Builder.Default
    private int availableQuantity = 0;

    @Column(nullable = false)
    @Builder.Default
    private int reservedQuantity = 0;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public int getTotalQuantity() {
        return availableQuantity + reservedQuantity;
    }
}
