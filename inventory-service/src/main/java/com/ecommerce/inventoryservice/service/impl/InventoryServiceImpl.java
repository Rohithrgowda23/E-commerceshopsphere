package com.ecommerce.inventoryservice.service.impl;

import com.ecommerce.inventoryservice.dto.request.AddStockRequest;
import com.ecommerce.inventoryservice.dto.request.ReleaseStockRequest;
import com.ecommerce.inventoryservice.dto.request.ReserveStockRequest;
import com.ecommerce.inventoryservice.dto.request.StockCheckRequest;
import com.ecommerce.inventoryservice.dto.response.InventoryResponse;
import com.ecommerce.inventoryservice.dto.response.StockCheckResponse;
import com.ecommerce.inventoryservice.entity.Inventory;
import com.ecommerce.inventoryservice.entity.StockReservation;
import com.ecommerce.inventoryservice.exception.InsufficientStockException;
import com.ecommerce.inventoryservice.exception.ResourceNotFoundException;
import com.ecommerce.inventoryservice.kafka.producer.InventoryEventProducer;
import com.ecommerce.inventoryservice.mapper.InventoryMapper;
import com.ecommerce.inventoryservice.repository.InventoryRepository;
import com.ecommerce.inventoryservice.repository.StockReservationRepository;
import com.ecommerce.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryRepository inventoryRepository;
    private final StockReservationRepository stockReservationRepository;
    private final InventoryMapper inventoryMapper;
    private final InventoryEventProducer inventoryEventProducer;

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventory(String productId) {
        Inventory inventory = findOrThrow(productId);
        return inventoryMapper.toResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public StockCheckResponse checkStock(StockCheckRequest request) {
        List<StockCheckResponse.ItemAvailability> results = request.getItems().stream()
                .map(item -> {
                    Inventory inventory = inventoryRepository.findById(item.getProductId()).orElse(null);
                    int available = inventory != null ? inventory.getAvailableQuantity() : 0;
                    boolean inStock = available >= item.getQuantity();
                    return StockCheckResponse.ItemAvailability.builder()
                            .productId(item.getProductId())
                            .requestedQuantity(item.getQuantity())
                            .availableQuantity(available)
                            .inStock(inStock)
                            .build();
                })
                .collect(Collectors.toList());

        boolean allInStock = results.stream().allMatch(StockCheckResponse.ItemAvailability::isInStock);

        return StockCheckResponse.builder()
                .allInStock(allInStock)
                .items(results)
                .build();
    }

    @Override
    @Transactional
    public void reserveStock(ReserveStockRequest request) {
        // Idempotency: a redelivered reserve request for an order that
        // already has reservations is a no-op success, not a double-reserve.
        if (stockReservationRepository.existsByOrderId(request.getOrderId())) {
            log.info("Stock already reserved for orderId={}, skipping duplicate request", request.getOrderId());
            return;
        }

        for (StockCheckRequest.StockItem item : request.getItems()) {
            Inventory inventory = inventoryRepository.findByIdForUpdate(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No inventory record for productId: " + item.getProductId()));

            if (inventory.getAvailableQuantity() < item.getQuantity()) {
                log.warn("Insufficient stock for orderId={} productId={} requested={} available={}",
                        request.getOrderId(), item.getProductId(), item.getQuantity(), inventory.getAvailableQuantity());
                inventoryEventProducer.publishReservationFailed(request.getOrderId(),
                        "Insufficient stock for productId: " + item.getProductId());
                throw new InsufficientStockException(
                        "Insufficient stock for productId: " + item.getProductId());
            }

            inventory.setAvailableQuantity(inventory.getAvailableQuantity() - item.getQuantity());
            inventory.setReservedQuantity(inventory.getReservedQuantity() + item.getQuantity());
            inventoryRepository.save(inventory);

            stockReservationRepository.save(StockReservation.builder()
                    .orderId(request.getOrderId())
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .build());
        }

        log.info("Reserved stock for orderId={}", request.getOrderId());
        inventoryEventProducer.publishReserved(request.getOrderId());
    }

    @Override
    @Transactional
    public void releaseStock(ReleaseStockRequest request) {
        List<StockReservation> reservations = stockReservationRepository.findByOrderId(request.getOrderId());

        if (reservations.isEmpty()) {
            log.info("No active reservation found for orderId={}, nothing to release", request.getOrderId());
            return;
        }

        for (StockReservation reservation : reservations) {
            Inventory inventory = inventoryRepository.findByIdForUpdate(reservation.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No inventory record for productId: " + reservation.getProductId()));

            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + reservation.getQuantity());
            inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - reservation.getQuantity()));
            inventoryRepository.save(inventory);
        }

        stockReservationRepository.deleteByOrderId(request.getOrderId());
        log.info("Released stock for orderId={}", request.getOrderId());
        inventoryEventProducer.publishReleased(request.getOrderId());
    }

    @Override
    @Transactional
    public void reduceStock(String orderId) {
        // Called after payment succeeds: converts a reservation into a
        // permanent deduction (reservedQuantity is cleared, availableQuantity
        // was already decremented at reserve time so it does not change here).
        List<StockReservation> reservations = stockReservationRepository.findByOrderId(orderId);

        for (StockReservation reservation : reservations) {
            Inventory inventory = inventoryRepository.findByIdForUpdate(reservation.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No inventory record for productId: " + reservation.getProductId()));

            inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - reservation.getQuantity()));
            inventoryRepository.save(inventory);
        }

        stockReservationRepository.deleteByOrderId(orderId);
        log.info("Permanently deducted stock for orderId={}", orderId);
    }

    @Override
    @Transactional
    public InventoryResponse addStock(AddStockRequest request) {
        Inventory inventory = inventoryRepository.findByIdForUpdate(request.getProductId())
                .orElseGet(() -> Inventory.builder().productId(request.getProductId()).build());

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + request.getQuantity());
        Inventory saved = inventoryRepository.save(inventory);

        log.info("Added {} units to productId={}, new availableQuantity={}",
                request.getQuantity(), request.getProductId(), saved.getAvailableQuantity());
        return inventoryMapper.toResponse(saved);
    }

    private Inventory findOrThrow(String productId) {
        return inventoryRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("No inventory record for productId: " + productId));
    }
}
