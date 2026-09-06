package com.ecommerce.notificationservice.kafka.consumer;

import com.ecommerce.notificationservice.entity.NotificationType;
import com.ecommerce.notificationservice.kafka.event.UserEvent;
import com.ecommerce.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserEventConsumer.class);

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "user-events",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleUserEvent(UserEvent event) {
        log.info("Received user event eventId={} type={} userId={}",
                event.getEventId(), event.getEventType(), event.getUserId());

        if (!"USER_REGISTERED".equals(event.getEventType())) {
            return;
        }

        notificationService.logNotification(
                event.getEventId(),
                NotificationType.USER_REGISTERED,
                event.getUserId(),
                null,
                "Welcome to the store!",
                "Hi " + event.getFirstName() + ", thanks for creating an account with us.");
    }
}
