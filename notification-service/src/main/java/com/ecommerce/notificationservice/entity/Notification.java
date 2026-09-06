package com.ecommerce.notificationservice.entity;

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
 * Initially just logs notifications and stores them in MySQL (per the
 * project spec) rather than actually sending email/SMS/push — the
 * "channel" field and this entity shape are what a real send-integration
 * would plug into later without changing the Kafka consumers that create
 * these rows.
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_user", columnList = "user_id"),
        @Index(name = "idx_notifications_order", columnList = "order_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Column(name = "user_id")
    private String userId;

    @Column(name = "order_id")
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String channel = "EMAIL";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.LOGGED;

    // Source Kafka event id — lets a redelivered event be recognized and
    // skipped instead of creating a duplicate notification.
    @Column(name = "source_event_id", unique = true)
    private String sourceEventId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
