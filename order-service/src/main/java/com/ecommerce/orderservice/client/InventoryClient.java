package com.ecommerce.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service", configuration = com.ecommerce.orderservice.config.FeignConfig.class)
public interface InventoryClient {

    @PostMapping("/api/inventory/check")
    InventoryClientDtos.StockCheckResponse checkStock(@RequestBody InventoryClientDtos.StockCheckRequest request);

    @PostMapping("/api/inventory/reserve")
    void reserveStock(@RequestBody InventoryClientDtos.ReserveStockRequest request);

    @PostMapping("/api/inventory/release")
    void releaseStock(@RequestBody InventoryClientDtos.ReleaseStockRequest request);
}
