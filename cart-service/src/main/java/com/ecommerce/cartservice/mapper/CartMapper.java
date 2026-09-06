package com.ecommerce.cartservice.mapper;

import com.ecommerce.cartservice.client.ProductClientResponse;
import com.ecommerce.cartservice.dto.response.CartItemResponse;
import com.ecommerce.cartservice.entity.CartItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CartMapper {

    public CartItemResponse toItemResponse(CartItem item, ProductClientResponse product) {
        BigDecimal unitPrice = resolvePrice(product);
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponse.builder()
                .productId(item.getProductId())
                .productName(product.getName())
                .imageUrl(product.getImageUrl())
                .unitPrice(unitPrice)
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .available(product.isAvailable())
                .build();
    }

    private BigDecimal resolvePrice(ProductClientResponse product) {
        if (product.getDiscountPrice() != null) {
            return product.getDiscountPrice();
        }
        return product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
    }
}
