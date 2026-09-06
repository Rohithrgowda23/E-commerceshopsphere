package com.ecommerce.cartservice.service.impl;

import com.ecommerce.cartservice.client.ProductClient;
import com.ecommerce.cartservice.client.ProductClientResponse;
import com.ecommerce.cartservice.dto.request.AddCartItemRequest;
import com.ecommerce.cartservice.dto.request.UpdateCartItemRequest;
import com.ecommerce.cartservice.dto.response.CartItemResponse;
import com.ecommerce.cartservice.dto.response.CartResponse;
import com.ecommerce.cartservice.entity.Cart;
import com.ecommerce.cartservice.entity.CartItem;
import com.ecommerce.cartservice.exception.ProductUnavailableException;
import com.ecommerce.cartservice.exception.ResourceNotFoundException;
import com.ecommerce.cartservice.mapper.CartMapper;
import com.ecommerce.cartservice.repository.CartRepository;
import com.ecommerce.cartservice.service.CartService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final ProductClient productClient;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String userId) {
        Cart cart = findOrCreateCart(userId);
        return toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(String userId, AddCartItemRequest request) {
        ProductClientResponse product = productClient.getProduct(request.getProductId());
        if (!product.isAvailable()) {
            throw new ProductUnavailableException(
                    "Product is not currently available: " + request.getProductId());
        }

        Cart cart = findOrCreateCart(userId);
        CartItem newItem = CartItem.builder()
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .build();
        cart.addOrUpdateItem(newItem);

        Cart saved = cartRepository.save(cart);
        log.info("Added productId={} qty={} to cart for userId={}",
                request.getProductId(), request.getQuantity(), userId);
        return toCartResponse(saved);
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(String userId, String productId, UpdateCartItemRequest request) {
        Cart cart = findOrCreateCart(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found in cart: " + productId));

        item.setQuantity(request.getQuantity());
        Cart saved = cartRepository.save(cart);
        log.info("Updated productId={} to qty={} for userId={}", productId, request.getQuantity(), userId);
        return toCartResponse(saved);
    }

    @Override
    @Transactional
    public CartResponse removeItem(String userId, String productId) {
        Cart cart = findOrCreateCart(userId);

        if (!cart.removeItem(productId)) {
            throw new ResourceNotFoundException("Product not found in cart: " + productId);
        }

        Cart saved = cartRepository.save(cart);
        log.info("Removed productId={} from cart for userId={}", productId, userId);
        return toCartResponse(saved);
    }

    @Override
    @Transactional
    public void clearCart(String userId) {
        Cart cart = findOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
        log.info("Cleared cart for userId={}", userId);
    }

    private Cart findOrCreateCart(String userId) {
        return cartRepository.findById(userId)
                .orElseGet(() -> Cart.builder().userId(userId).build());
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> {
                    ProductClientResponse product = productClient.getProduct(item.getProductId());
                    return cartMapper.toItemResponse(item, product);
                })
                .collect(Collectors.toList());

        BigDecimal total = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .userId(cart.getUserId())
                .items(itemResponses)
                .total(total)
                .build();
    }
}
