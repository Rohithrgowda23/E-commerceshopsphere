package com.ecommerce.paymentservice.kafka.producer;

import com.ecommerce.paymentservice.kafka.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);
    private static final String TOPIC = "payment-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentSuccess(String orderId, String paymentId) {
        publish(orderId, paymentId, "PAYMENT_SUCCESS", null);
    }

    public void publishPaymentFailed(String orderId, String paymentId, String reason) {
        publish(orderId, paymentId, "PAYMENT_FAILED", reason);
    }

    private void publish(String orderId, String paymentId, String eventType, String reason) {
        PaymentEvent event = PaymentEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .timestamp(LocalDateTime.now())
                .orderId(orderId)
                .paymentId(paymentId)
                .reason(reason)
                .build();

        kafkaTemplate.send(TOPIC, orderId, event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish {} for orderId={}: {}", eventType, orderId, ex.getMessage(), ex);
            } else {
                log.info("Published {} for orderId={} to partition={} offset={}",
                        eventType, orderId,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
