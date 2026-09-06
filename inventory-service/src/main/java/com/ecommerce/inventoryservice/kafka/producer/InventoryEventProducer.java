package com.ecommerce.inventoryservice.kafka.producer;

import com.ecommerce.inventoryservice.kafka.event.InventoryReleasedEvent;
import com.ecommerce.inventoryservice.kafka.event.InventoryReservationFailedEvent;
import com.ecommerce.inventoryservice.kafka.event.InventoryReservedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventProducer.class);
    private static final String TOPIC = "inventory-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishReserved(String orderId) {
        InventoryReservedEvent event = InventoryReservedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("INVENTORY_RESERVED")
                .timestamp(LocalDateTime.now())
                .orderId(orderId)
                .build();
        send(orderId, event, "INVENTORY_RESERVED");
    }

    public void publishReleased(String orderId) {
        InventoryReleasedEvent event = InventoryReleasedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("INVENTORY_RELEASED")
                .timestamp(LocalDateTime.now())
                .orderId(orderId)
                .build();
        send(orderId, event, "INVENTORY_RELEASED");
    }

    public void publishReservationFailed(String orderId, String reason) {
        InventoryReservationFailedEvent event = InventoryReservationFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("INVENTORY_RESERVATION_FAILED")
                .timestamp(LocalDateTime.now())
                .orderId(orderId)
                .reason(reason)
                .build();
        send(orderId, event, "INVENTORY_RESERVATION_FAILED");
    }

    private void send(String key, Object event, String eventType) {
        kafkaTemplate.send(TOPIC, key, event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish {} for key={}: {}", eventType, key, ex.getMessage(), ex);
            } else {
                log.info("Published {} for key={} to partition={} offset={}",
                        eventType, key,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
