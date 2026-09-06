package com.ecommerce.orderservice.kafka.producer;

import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.kafka.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);
    private static final String TOPIC = "order-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(Order order) {
        publish(order, "ORDER_CREATED");
    }

    public void publishOrderCancelled(Order order) {
        publish(order, "ORDER_CANCELLED");
    }

    public void publishOrderStatusChanged(Order order) {
        publish(order, "ORDER_STATUS_CHANGED");
    }

    private void publish(Order order, String eventType) {
        List<OrderEvent.OrderItemPayload> items = order.getItems().stream()
                .map(item -> OrderEvent.OrderItemPayload.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        OrderEvent event = OrderEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .timestamp(LocalDateTime.now())
                .orderId(order.getId())
                .userId(order.getUserId())
                .amount(order.getTotalAmount())
                .items(items)
                .build();

        kafkaTemplate.send(TOPIC, order.getId(), event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish {} for orderId={}: {}", eventType, order.getId(), ex.getMessage(), ex);
            } else {
                log.info("Published {} for orderId={} to partition={} offset={}",
                        eventType, order.getId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
