package com.ecommerce.cartservice.service;

import com.ecommerce.cartservice.client.ProductClient;
import com.ecommerce.cartservice.client.ProductClientResponse;
import com.ecommerce.cartservice.dto.request.AddCartItemRequest;
import com.ecommerce.cartservice.dto.request.UpdateCartItemRequest;
import com.ecommerce.cartservice.dto.response.CartResponse;
import com.ecommerce.cartservice.entity.Cart;
import com.ecommerce.cartservice.entity.CartItem;
import com.ecommerce.cartservice.exception.ProductUnavailableException;
import com.ecommerce.cartservice.exception.ResourceNotFoundException;
import com.ecommerce.cartservice.mapper.CartMapper;
import com.ecommerce.cartservice.repository.CartRepository;
import com.ecommerce.cartservice.service.impl.CartServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductClient productClient;
    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void addItem_availableProduct_addsToNewCart() {
        AddCartItemRequest request = new AddCartItemRequest("p1", 2);
        ProductClientResponse product = new ProductClientResponse("p1", "Widget", BigDecimal.TEN, null, "img", true);

        when(productClient.getProduct("p1")).thenReturn(product);
        when(cartRepository.findById("user-1")).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartMapper.toItemResponse(any(CartItem.class), eq(product)))
                .thenReturn(com.ecommerce.cartservice.dto.response.CartItemResponse.builder()
                        .productId("p1").quantity(2).subtotal(BigDecimal.valueOf(20)).build());

        CartResponse response = cartService.addItem("user-1", request);

        assertEquals(1, response.getItems().size());
        assertEquals(BigDecimal.valueOf(20), response.getTotal());
    }

    @Test
    void addItem_unavailableProduct_throwsProductUnavailable() {
        AddCartItemRequest request = new AddCartItemRequest("p1", 2);
        ProductClientResponse product = new ProductClientResponse("p1", "Widget", BigDecimal.TEN, null, "img", false);

        when(productClient.getProduct("p1")).thenReturn(product);

        assertThrows(ProductUnavailableException.class, () -> cartService.addItem("user-1", request));
    }

    @Test
    void updateItemQuantity_itemNotInCart_throwsResourceNotFound() {
        Cart cart = Cart.builder().userId("user-1").items(List.of()).build();
        when(cartRepository.findById("user-1")).thenReturn(Optional.of(cart));

        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.updateItemQuantity("user-1", "missing-product", request));
    }

    @Test
    void removeItem_itemNotInCart_throwsResourceNotFound() {
        Cart cart = Cart.builder().userId("user-1").items(new java.util.ArrayList<>()).build();
        when(cartRepository.findById("user-1")).thenReturn(Optional.of(cart));

        assertThrows(ResourceNotFoundException.class, () -> cartService.removeItem("user-1", "missing-product"));
    }

    @Test
    void getCart_noExistingCart_returnsEmptyCart() {
        when(cartRepository.findById("user-1")).thenReturn(Optional.empty());

        CartResponse response = cartService.getCart("user-1");

        assertEquals(0, response.getItems().size());
        assertEquals(BigDecimal.ZERO, response.getTotal());
    }
}
