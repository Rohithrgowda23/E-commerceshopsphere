package com.ecommerce.notificationservice.service.impl;

import com.ecommerce.notificationservice.dto.response.NotificationResponse;
import com.ecommerce.notificationservice.dto.response.PagedResponse;
import com.ecommerce.notificationservice.entity.Notification;
import com.ecommerce.notificationservice.entity.NotificationStatus;
import com.ecommerce.notificationservice.entity.NotificationType;
import com.ecommerce.notificationservice.mapper.NotificationMapper;
import com.ecommerce.notificationservice.repository.NotificationRepository;
import com.ecommerce.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void logNotification(String sourceEventId, NotificationType type, String userId, String orderId,
                                 String title, String message) {
        if (sourceEventId != null && notificationRepository.existsBySourceEventId(sourceEventId)) {
            log.info("Notification for sourceEventId={} already logged, skipping duplicate", sourceEventId);
            return;
        }

        Notification notification = Notification.builder()
                .userId(userId)
                .orderId(orderId)
                .type(type)
                .title(title)
                .message(message)
                .channel("EMAIL")
                .status(NotificationStatus.LOGGED)
                .sourceEventId(sourceEventId)
                .build();

        notificationRepository.save(notification);
        // "Sending" is simulated by logging — a real integration (SES,
        // Twilio, FCM, etc.) would be called here and the status updated
        // to SENT/FAILED based on the result.
        log.info("[NOTIFICATION] to userId={} type={} title=\"{}\" message=\"{}\"",
                userId, type, title, message);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getNotificationsForUser(String userId, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);

        Page<Notification> notifications = notificationRepository.findByUserId(
                userId, PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")));

        return PagedResponse.from(notifications.map(notificationMapper::toResponse));
    }
}
