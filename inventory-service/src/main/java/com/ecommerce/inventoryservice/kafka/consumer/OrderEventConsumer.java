package com.ecommerce.inventoryservice.kafka.consumer;

import com.ecommerce.inventoryservice.dto.request.ReleaseStockRequest;
import com.ecommerce.inventoryservice.dto.request.ReserveStockRequest;
import com.ecommerce.inventoryservice.dto.request.StockCheckRequest;
import com.ecommerce.inventoryservice.exception.InsufficientStockException;
import com.ecommerce.inventoryservice.kafka.event.OrderEvent;
import com.ecommerce.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Reacts to order lifecycle events published by order-service:
 *  - ORDER_CREATED  -> reserve stock for the order's items
 *  - ORDER_CANCELLED -> release any stock previously reserved for the order
 *
 * This is the asynchronous half of the reservation flow (see the
 * order-flow diagram in the Phase 0 architecture doc); order-service can
 * additionally call POST /api/inventory/reserve synchronously via
 * OpenFeign when it needs an immediate answer before returning to the
 * client — both paths are idempotent, so whichever runs first "wins"
 * and the other becomes a no-op.
 */
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final InventoryService inventoryService;

    @KafkaListener(
            topics = "order-events",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received order event eventId={} type={} orderId={}",
                event.getEventId(), event.getEventType(), event.getOrderId());

        switch (event.getEventType()) {
            case "ORDER_CREATED" -> handleOrderCreated(event);
            case "ORDER_CANCELLED" -> handleOrderCancelled(event);
            default -> log.debug("Ignoring event type={} (not handled by inventory-service)", event.getEventType());
        }
    }

    private void handleOrderCreated(OrderEvent event) {
        List<StockCheckRequest.StockItem> items = toStockItems(event);
        try {
            inventoryService.reserveStock(new ReserveStockRequest(event.getOrderId(), items));
        } catch (InsufficientStockException e) {
            // Already published an INVENTORY_RESERVATION_FAILED event inside
            // reserveStock(); order-service consumes that to cancel the order.
            log.warn("Reservation failed for orderId={}: {}", event.getOrderId(), e.getMessage());
        }
    }

    private void handleOrderCancelled(OrderEvent event) {
        List<StockCheckRequest.StockItem> items = toStockItems(event);
        inventoryService.releaseStock(new ReleaseStockRequest(event.getOrderId(), items));
    }

    private List<StockCheckRequest.StockItem> toStockItems(OrderEvent event) {
        return event.getItems().stream()
                .map(i -> new StockCheckRequest.StockItem(i.getProductId(), i.getQuantity()))
                .collect(Collectors.toList());
    }
}
