package com.ecommerce.notificationservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;

    private String orderId;
    private String reason;
}
