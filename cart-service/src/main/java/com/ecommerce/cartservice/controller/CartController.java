package com.ecommerce.cartservice.controller;

import com.ecommerce.cartservice.dto.request.AddCartItemRequest;
import com.ecommerce.cartservice.dto.request.UpdateCartItemRequest;
import com.ecommerce.cartservice.dto.response.CartResponse;
import com.ecommerce.cartservice.security.SecurityUtils;
import com.ecommerce.cartservice.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "The authenticated user's shopping cart")
public class CartController {

    private final CartService cartService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "Get the current user's cart")
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart(securityUtils.getCurrentUserId()));
    }

    @PostMapping("/items")
    @Operation(summary = "Add an item to the cart (merges quantity if it already exists)")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(securityUtils.getCurrentUserId(), request));
    }

    @PutMapping("/items/{productId}")
    @Operation(summary = "Set an item's quantity")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable String productId, @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItemQuantity(securityUtils.getCurrentUserId(), productId, request));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove an item from the cart")
    public ResponseEntity<CartResponse> removeItem(@PathVariable String productId) {
        return ResponseEntity.ok(cartService.removeItem(securityUtils.getCurrentUserId(), productId));
    }

    @DeleteMapping
    @Operation(summary = "Clear the entire cart")
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart(securityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
