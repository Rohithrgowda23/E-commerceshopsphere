package com.ecommerce.notificationservice.kafka.consumer;

import com.ecommerce.notificationservice.entity.NotificationType;
import com.ecommerce.notificationservice.kafka.event.PaymentEvent;
import com.ecommerce.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "payment-events",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Received payment event eventId={} type={} orderId={}",
                event.getEventId(), event.getEventType(), event.getOrderId());

        switch (event.getEventType()) {
            case "PAYMENT_SUCCESS" -> notificationService.logNotification(
                    event.getEventId(), NotificationType.PAYMENT_SUCCESS, null, event.getOrderId(),
                    "Payment successful",
                    "Payment for order " + event.getOrderId() + " was successful.");
            case "PAYMENT_FAILED" -> notificationService.logNotification(
                    event.getEventId(), NotificationType.PAYMENT_FAILED, null, event.getOrderId(),
                    "Payment failed",
                    "Payment for order " + event.getOrderId() + " failed: " + event.getReason());
            default -> log.debug("Ignoring payment event type={}", event.getEventType());
        }
    }
}
