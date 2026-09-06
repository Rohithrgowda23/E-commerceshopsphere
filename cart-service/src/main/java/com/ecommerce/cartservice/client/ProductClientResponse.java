package com.ecommerce.cartservice.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Mirrors the subset of product-service's ProductResponse that cart-service
 * actually needs. Kept as a separate, narrower contract here rather than
 * sharing product-service's DTO class, so each service stays independently
 * deployable and product-service is free to add fields without breaking
 * this client's deserialization.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductClientResponse {
    private String id;
    private String name;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String imageUrl;
    private boolean available;
}
