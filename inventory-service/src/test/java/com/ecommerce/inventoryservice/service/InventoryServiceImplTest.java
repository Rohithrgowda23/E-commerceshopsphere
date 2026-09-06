package com.ecommerce.inventoryservice.service;

import com.ecommerce.inventoryservice.dto.request.AddStockRequest;
import com.ecommerce.inventoryservice.dto.request.ReleaseStockRequest;
import com.ecommerce.inventoryservice.dto.request.ReserveStockRequest;
import com.ecommerce.inventoryservice.dto.request.StockCheckRequest;
import com.ecommerce.inventoryservice.dto.response.InventoryResponse;
import com.ecommerce.inventoryservice.entity.Inventory;
import com.ecommerce.inventoryservice.entity.StockReservation;
import com.ecommerce.inventoryservice.exception.InsufficientStockException;
import com.ecommerce.inventoryservice.exception.ResourceNotFoundException;
import com.ecommerce.inventoryservice.kafka.producer.InventoryEventProducer;
import com.ecommerce.inventoryservice.mapper.InventoryMapper;
import com.ecommerce.inventoryservice.repository.InventoryRepository;
import com.ecommerce.inventoryservice.repository.StockReservationRepository;
import com.ecommerce.inventoryservice.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private StockReservationRepository stockReservationRepository;
    @Mock
    private InventoryMapper inventoryMapper;
    @Mock
    private InventoryEventProducer inventoryEventProducer;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    void reserveStock_sufficientStock_reservesSuccessfully() {
        ReserveStockRequest request = new ReserveStockRequest(
                "order-1", List.of(new StockCheckRequest.StockItem("p1", 2)));

        Inventory inventory = Inventory.builder().productId("p1").availableQuantity(10).reservedQuantity(0).build();

        when(stockReservationRepository.existsByOrderId("order-1")).thenReturn(false);
        when(inventoryRepository.findByIdForUpdate("p1")).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockReservationRepository.save(any(StockReservation.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        inventoryService.reserveStock(request);

        assertEquals(8, inventory.getAvailableQuantity());
        assertEquals(2, inventory.getReservedQuantity());
        verify(inventoryEventProducer).publishReserved("order-1");
    }

    @Test
    void reserveStock_insufficientStock_throwsAndPublishesFailure() {
        ReserveStockRequest request = new ReserveStockRequest(
                "order-2", List.of(new StockCheckRequest.StockItem("p1", 100)));

        Inventory inventory = Inventory.builder().productId("p1").availableQuantity(5).reservedQuantity(0).build();

        when(stockReservationRepository.existsByOrderId("order-2")).thenReturn(false);
        when(inventoryRepository.findByIdForUpdate("p1")).thenReturn(Optional.of(inventory));

        assertThrows(InsufficientStockException.class, () -> inventoryService.reserveStock(request));
        verify(inventoryEventProducer).publishReservationFailed("order-2", any());
        verify(inventoryEventProducer, never()).publishReserved(any());
    }

    @Test
    void reserveStock_alreadyReservedForOrder_isIdempotentNoOp() {
        ReserveStockRequest request = new ReserveStockRequest(
                "order-3", List.of(new StockCheckRequest.StockItem("p1", 2)));

        when(stockReservationRepository.existsByOrderId("order-3")).thenReturn(true);

        inventoryService.reserveStock(request);

        verify(inventoryRepository, never()).findByIdForUpdate(any());
        verify(inventoryEventProducer, never()).publishReserved(any());
    }

    @Test
    void releaseStock_existingReservation_restoresAvailableQuantity() {
        ReleaseStockRequest request = new ReleaseStockRequest(
                "order-4", List.of(new StockCheckRequest.StockItem("p1", 3)));

        StockReservation reservation = StockReservation.builder()
                .orderId("order-4").productId("p1").quantity(3).build();
        Inventory inventory = Inventory.builder().productId("p1").availableQuantity(5).reservedQuantity(3).build();

        when(stockReservationRepository.findByOrderId("order-4")).thenReturn(List.of(reservation));
        when(inventoryRepository.findByIdForUpdate("p1")).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));

        inventoryService.releaseStock(request);

        assertEquals(8, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
        verify(stockReservationRepository).deleteByOrderId("order-4");
        verify(inventoryEventProducer).publishReleased("order-4");
    }

    @Test
    void releaseStock_noReservationFound_isNoOp() {
        ReleaseStockRequest request = new ReleaseStockRequest(
                "order-5", List.of(new StockCheckRequest.StockItem("p1", 3)));

        when(stockReservationRepository.findByOrderId("order-5")).thenReturn(List.of());

        inventoryService.releaseStock(request);

        verify(inventoryRepository, never()).findByIdForUpdate(any());
        verify(inventoryEventProducer, never()).publishReleased(any());
    }

    @Test
    void addStock_newProduct_createsInventoryRecord() {
        AddStockRequest request = new AddStockRequest("p2", 50);

        when(inventoryRepository.findByIdForUpdate("p2")).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inventoryMapper.toResponse(any(Inventory.class)))
                .thenReturn(InventoryResponse.builder().productId("p2").availableQuantity(50).build());

        InventoryResponse response = inventoryService.addStock(request);

        assertEquals(50, response.getAvailableQuantity());
    }

    @Test
    void getInventory_unknownProduct_throwsResourceNotFound() {
        when(inventoryRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryService.getInventory("missing"));
    }
}
