package com.ecommerce.inventoryservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockCheckResponse {

    private boolean allInStock;
    private List<ItemAvailability> items;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemAvailability {
        private String productId;
        private int requestedQuantity;
        private int availableQuantity;
        private boolean inStock;
    }
}
