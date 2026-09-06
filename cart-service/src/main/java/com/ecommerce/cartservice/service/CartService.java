package com.ecommerce.cartservice.service;

import com.ecommerce.cartservice.dto.request.AddCartItemRequest;
import com.ecommerce.cartservice.dto.request.UpdateCartItemRequest;
import com.ecommerce.cartservice.dto.response.CartResponse;

public interface CartService {

    CartResponse getCart(String userId);

    CartResponse addItem(String userId, AddCartItemRequest request);

    CartResponse updateItemQuantity(String userId, String productId, UpdateCartItemRequest request);

    CartResponse removeItem(String userId, String productId);

    void clearCart(String userId);
}
