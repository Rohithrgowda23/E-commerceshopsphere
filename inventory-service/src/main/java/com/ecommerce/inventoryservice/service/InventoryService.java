package com.ecommerce.inventoryservice.service;

import com.ecommerce.inventoryservice.dto.request.AddStockRequest;
import com.ecommerce.inventoryservice.dto.request.ReleaseStockRequest;
import com.ecommerce.inventoryservice.dto.request.ReserveStockRequest;
import com.ecommerce.inventoryservice.dto.request.StockCheckRequest;
import com.ecommerce.inventoryservice.dto.response.InventoryResponse;
import com.ecommerce.inventoryservice.dto.response.StockCheckResponse;

public interface InventoryService {

    InventoryResponse getInventory(String productId);

    StockCheckResponse checkStock(StockCheckRequest request);

    void reserveStock(ReserveStockRequest request);

    void releaseStock(ReleaseStockRequest request);

    void reduceStock(String orderId);

    InventoryResponse addStock(AddStockRequest request);
}
