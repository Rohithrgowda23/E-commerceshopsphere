package com.ecommerce.userservice.kafka.consumer;

import com.ecommerce.userservice.entity.UserProfile;
import com.ecommerce.userservice.kafka.event.UserRegisteredEvent;
import com.ecommerce.userservice.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserEventConsumer.class);

    private final UserProfileRepository userProfileRepository;

    @KafkaListener(
            topics = "user-events",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Received event eventId={} type={} userId={}",
                event.getEventId(), event.getEventType(), event.getUserId());

        if (!"USER_REGISTERED".equals(event.getEventType())) {
            log.debug("Ignoring event type={} (not handled by user-service)", event.getEventType());
            return;
        }

        // Idempotency: a redelivered message must not create a duplicate profile.
        if (userProfileRepository.existsByUserId(event.getUserId())) {
            log.info("Profile already exists for userId={}, skipping duplicate event eventId={}",
                    event.getUserId(), event.getEventId());
            return;
        }

        UserProfile profile = UserProfile.builder()
                .userId(event.getUserId())
                .email(event.getEmail())
                .firstName(event.getFirstName())
                .lastName(event.getLastName())
                .build();

        userProfileRepository.save(profile);
        log.info("Created user profile for userId={}", event.getUserId());
    }
}
