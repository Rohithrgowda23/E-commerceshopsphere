package com.ecommerce.notificationservice.service;

import com.ecommerce.notificationservice.dto.response.NotificationResponse;
import com.ecommerce.notificationservice.dto.response.PagedResponse;
import com.ecommerce.notificationservice.entity.NotificationType;

public interface NotificationService {

    /**
     * Idempotently logs a notification — a redelivered Kafka event with
     * the same sourceEventId is a no-op rather than a duplicate row.
     */
    void logNotification(String sourceEventId, NotificationType type, String userId, String orderId,
                          String title, String message);

    PagedResponse<NotificationResponse> getNotificationsForUser(String userId, int page, int size);
}
