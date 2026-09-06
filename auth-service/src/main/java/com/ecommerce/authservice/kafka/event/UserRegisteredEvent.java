package com.ecommerce.authservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Published to the "user-events" Kafka topic after a user successfully
 * registers. user-service consumes this to create the initial profile row;
 * notification-service consumes it to send a welcome notification.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;

    private String userId;
    private String email;
    private String firstName;
    private String lastName;
}
