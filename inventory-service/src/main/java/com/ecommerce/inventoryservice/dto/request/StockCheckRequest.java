package com.ecommerce.inventoryservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockCheckRequest {

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<StockItem> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockItem {
        private String productId;
        private int quantity;
    }
}
