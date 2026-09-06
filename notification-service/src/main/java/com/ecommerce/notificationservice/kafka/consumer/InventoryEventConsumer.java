package com.ecommerce.notificationservice.kafka.consumer;

import com.ecommerce.notificationservice.entity.NotificationType;
import com.ecommerce.notificationservice.kafka.event.InventoryEvent;
import com.ecommerce.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "inventory-events",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleInventoryEvent(InventoryEvent event) {
        if (!"INVENTORY_RESERVATION_FAILED".equals(event.getEventType())) {
            return;
        }

        log.info("Received INVENTORY_RESERVATION_FAILED eventId={} orderId={}",
                event.getEventId(), event.getOrderId());

        notificationService.logNotification(
                event.getEventId(), NotificationType.INVENTORY_RESERVATION_FAILED, null, event.getOrderId(),
                "Item unavailable",
                "We couldn't reserve stock for order " + event.getOrderId() + ": " + event.getReason());
    }
}
