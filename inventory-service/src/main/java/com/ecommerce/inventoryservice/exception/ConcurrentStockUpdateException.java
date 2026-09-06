package com.ecommerce.inventoryservice.exception;

public class ConcurrentStockUpdateException extends RuntimeException {
    public ConcurrentStockUpdateException(String message) {
        super(message);
    }
}
