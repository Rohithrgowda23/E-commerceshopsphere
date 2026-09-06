package com.ecommerce.inventoryservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddStockRequest {

    @NotBlank(message = "Product id is required")
    private String productId;

    @Positive(message = "Quantity must be positive")
    private int quantity;
}
