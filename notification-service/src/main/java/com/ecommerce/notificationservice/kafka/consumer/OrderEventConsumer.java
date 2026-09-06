package com.ecommerce.notificationservice.kafka.consumer;

import com.ecommerce.notificationservice.entity.NotificationType;
import com.ecommerce.notificationservice.kafka.event.OrderEvent;
import com.ecommerce.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "order-events",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received order event eventId={} type={} orderId={}",
                event.getEventId(), event.getEventType(), event.getOrderId());

        switch (event.getEventType()) {
            case "ORDER_CREATED" -> notificationService.logNotification(
                    event.getEventId(), NotificationType.ORDER_CREATED, event.getUserId(), event.getOrderId(),
                    "Order placed",
                    "Your order " + event.getOrderId() + " has been placed for " + event.getAmount() + ".");
            case "ORDER_CANCELLED" -> notificationService.logNotification(
                    event.getEventId(), NotificationType.ORDER_CANCELLED, event.getUserId(), event.getOrderId(),
                    "Order cancelled",
                    "Your order " + event.getOrderId() + " has been cancelled.");
            case "ORDER_STATUS_CHANGED" -> notificationService.logNotification(
                    event.getEventId(), NotificationType.ORDER_STATUS_CHANGED, event.getUserId(), event.getOrderId(),
                    "Order update",
                    "Your order " + event.getOrderId() + " status has changed.");
            default -> log.debug("Ignoring order event type={}", event.getEventType());
        }
    }
}
