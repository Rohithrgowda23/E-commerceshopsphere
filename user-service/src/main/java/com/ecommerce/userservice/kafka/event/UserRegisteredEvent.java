package com.ecommerce.userservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Mirrors the event shape published by auth-service to "user-events".
 * Each service keeps its own copy of the event contract rather than
 * sharing a library module, so every microservice remains independently
 * buildable/deployable.
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
