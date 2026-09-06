package com.ecommerce.orderservice.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class InventoryClientDtos {

    private InventoryClientDtos() {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockItem {
        private String productId;
        private int quantity;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockCheckRequest {
        private List<StockItem> items;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockCheckResponse {
        private boolean allInStock;
        private List<ItemAvailability> items;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ItemAvailability {
            private String productId;
            private int requestedQuantity;
            private int availableQuantity;
            private boolean inStock;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReserveStockRequest {
        private String orderId;
        private List<StockItem> items;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReleaseStockRequest {
        private String orderId;
        private List<StockItem> items;
    }
}
