package com.ecommerce.inventoryservice.controller;

import com.ecommerce.inventoryservice.dto.request.AddStockRequest;
import com.ecommerce.inventoryservice.dto.request.ReleaseStockRequest;
import com.ecommerce.inventoryservice.dto.request.ReserveStockRequest;
import com.ecommerce.inventoryservice.dto.request.StockCheckRequest;
import com.ecommerce.inventoryservice.dto.response.InventoryResponse;
import com.ecommerce.inventoryservice.dto.response.StockCheckResponse;
import com.ecommerce.inventoryservice.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Stock levels, availability checks, and reservation")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    @Operation(summary = "Get current stock for a product")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable String productId) {
        return ResponseEntity.ok(inventoryService.getInventory(productId));
    }

    @PostMapping("/check")
    @Operation(summary = "Check availability for one or more products (used by cart/checkout)")
    public ResponseEntity<StockCheckResponse> checkStock(@Valid @RequestBody StockCheckRequest request) {
        return ResponseEntity.ok(inventoryService.checkStock(request));
    }

    @PostMapping("/reserve")
    @Operation(summary = "Reserve stock for an order (idempotent per orderId)")
    public ResponseEntity<Void> reserveStock(@Valid @RequestBody ReserveStockRequest request) {
        inventoryService.reserveStock(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release")
    @Operation(summary = "Release previously reserved stock for an order")
    public ResponseEntity<Void> releaseStock(@Valid @RequestBody ReleaseStockRequest request) {
        inventoryService.releaseStock(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/add")
    @Operation(summary = "Add stock for a product (ADMIN only)")
    public ResponseEntity<InventoryResponse> addStock(@Valid @RequestBody AddStockRequest request) {
        return ResponseEntity.ok(inventoryService.addStock(request));
    }

    @PostMapping("/reduce/{orderId}")
    @Operation(summary = "Permanently deduct reserved stock after payment success (internal, authenticated)")
    public ResponseEntity<Void> reduceStock(@PathVariable String orderId) {
        inventoryService.reduceStock(orderId);
        return ResponseEntity.ok().build();
    }
}
