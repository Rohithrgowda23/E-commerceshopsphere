package com.ecommerce.paymentservice.kafka.consumer;

import com.ecommerce.paymentservice.dto.request.CreatePaymentRequest;
import com.ecommerce.paymentservice.kafka.event.OrderEvent;
import com.ecommerce.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Reacts to ORDER_CREATED on "order-events" by attempting a (mock) charge
 * automatically — this is the event-driven checkout flow: the customer
 * doesn't call POST /api/payments themselves, order creation triggers it.
 * The REST endpoint still exists for manual/administrative use and for
 * retrying a payment.
 */
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);
    private static final String DEFAULT_PAYMENT_METHOD = "CARD";

    private final PaymentService paymentService;

    @KafkaListener(
            topics = "order-events",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleOrderEvent(OrderEvent event) {
        if (!"ORDER_CREATED".equals(event.getEventType())) {
            return;
        }

        log.info("Received ORDER_CREATED for orderId={}, attempting payment", event.getOrderId());

        CreatePaymentRequest request = new CreatePaymentRequest(
                event.getOrderId(), event.getAmount(), DEFAULT_PAYMENT_METHOD);
        paymentService.processPayment(event.getUserId(), request);
    }
}
