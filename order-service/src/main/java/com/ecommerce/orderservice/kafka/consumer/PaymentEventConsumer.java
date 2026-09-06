package com.ecommerce.orderservice.kafka.consumer;

import com.ecommerce.orderservice.client.InventoryClient;
import com.ecommerce.orderservice.client.InventoryClientDtos;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.entity.PaymentStatus;
import com.ecommerce.orderservice.kafka.event.PaymentEvent;
import com.ecommerce.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Reacts to payment-service's outcome for an order:
 *  - PAYMENT_SUCCESS  -> order CONFIRMED, paymentStatus SUCCESS. Stock
 *    stays reserved->permanently deducted (inventory-service also
 *    consumes payment-events directly to do the deduction itself).
 *  - PAYMENT_FAILED   -> order CANCELLED, paymentStatus FAILED, and the
 *    reservation is released so the stock goes back on sale.
 */
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    @KafkaListener(
            topics = "payment-events",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Received payment event eventId={} type={} orderId={}",
                event.getEventId(), event.getEventType(), event.getOrderId());

        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) {
            log.warn("No order found for orderId={} from payment event eventId={}",
                    event.getOrderId(), event.getEventId());
            return;
        }

        switch (event.getEventType()) {
            case "PAYMENT_SUCCESS" -> handlePaymentSuccess(order);
            case "PAYMENT_FAILED" -> handlePaymentFailed(order);
            default -> log.debug("Ignoring payment event type={}", event.getEventType());
        }
    }

    private void handlePaymentSuccess(Order order) {
        // Idempotency: a redelivered PAYMENT_SUCCESS for an already-confirmed order is a no-op.
        if (order.getOrderStatus() == OrderStatus.CONFIRMED) {
            log.info("Order id={} already CONFIRMED, ignoring duplicate PAYMENT_SUCCESS", order.getId());
            return;
        }
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setOrderStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        log.info("Order id={} confirmed after successful payment", order.getId());
    }

    private void handlePaymentFailed(Order order) {
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            log.info("Order id={} already CANCELLED, ignoring duplicate PAYMENT_FAILED", order.getId());
            return;
        }
        order.setPaymentStatus(PaymentStatus.FAILED);
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        List<InventoryClientDtos.StockItem> items = order.getItems().stream()
                .map(i -> new InventoryClientDtos.StockItem(i.getProductId(), i.getQuantity()))
                .collect(Collectors.toList());
        inventoryClient.releaseStock(new InventoryClientDtos.ReleaseStockRequest(order.getId(), items));

        log.info("Order id={} cancelled after failed payment, stock released", order.getId());
    }
}
