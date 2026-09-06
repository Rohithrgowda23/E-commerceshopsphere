package com.ecommerce.orderservice.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartClientResponse {

    private String userId;
    private List<Item> items;
    private BigDecimal total;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String productId;
        private String productName;
        private String imageUrl;
        private BigDecimal unitPrice;
        private int quantity;
        private BigDecimal subtotal;
        private boolean available;
    }
}
