package com.ecommerce.notificationservice.mapper;

import com.ecommerce.notificationservice.dto.response.NotificationResponse;
import com.ecommerce.notificationservice.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .orderId(notification.getOrderId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .channel(notification.getChannel())
                .status(notification.getStatus().name())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
