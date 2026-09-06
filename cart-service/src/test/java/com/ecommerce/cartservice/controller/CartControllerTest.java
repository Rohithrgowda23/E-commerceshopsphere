package com.ecommerce.cartservice.controller;

import com.ecommerce.cartservice.dto.request.AddCartItemRequest;
import com.ecommerce.cartservice.dto.response.CartResponse;
import com.ecommerce.cartservice.security.JwtAuthenticationFilter;
import com.ecommerce.cartservice.security.SecurityUtils;
import com.ecommerce.cartservice.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getCart_returns200() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(cartService.getCart("user-1")).thenReturn(
                CartResponse.builder().userId("user-1").items(List.of()).total(BigDecimal.ZERO).build());

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk());
    }

    @Test
    void addItem_validRequest_returns200() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest("p1", 2);
        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(cartService.addItem(any(), any())).thenReturn(
                CartResponse.builder().userId("user-1").items(List.of()).total(BigDecimal.ZERO).build());

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void addItem_invalidQuantity_returns400() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest("p1", -1);

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
