package com.ecommerce.orderservice.kafka.consumer;

import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.kafka.event.InventoryEvent;
import com.ecommerce.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Safety net for the asynchronous reservation path: order-service's
 * synchronous Feign call to /api/inventory/reserve already catches the
 * common case at checkout time, but if that succeeded and a later,
 * independent async reservation attempt (e.g. a replay) fails, this
 * ensures the order doesn't stay stuck in an inconsistent state.
 */
@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final OrderRepository orderRepository;

    @KafkaListener(
            topics = "inventory-events",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void handleInventoryEvent(InventoryEvent event) {
        if (!"INVENTORY_RESERVATION_FAILED".equals(event.getEventType())) {
            return;
        }

        log.warn("Received INVENTORY_RESERVATION_FAILED for orderId={}: {}",
                event.getOrderId(), event.getReason());

        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null || order.getOrderStatus() == OrderStatus.CANCELLED) {
            return;
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order id={} cancelled due to inventory reservation failure", order.getId());
    }
}
