package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.request.ProductRequest;
import com.ecommerce.productservice.dto.response.PagedResponse;
import com.ecommerce.productservice.dto.response.ProductResponse;
import com.ecommerce.productservice.security.JwtAuthenticationFilter;
import com.ecommerce.productservice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getProduct_noAuth_returns200() throws Exception {
        when(productService.getProduct("p1")).thenReturn(ProductResponse.builder().id("p1").build());

        mockMvc.perform(get("/api/products/p1"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllProducts_noAuth_returns200() throws Exception {
        when(productService.getAllProducts(0, 20, "createdAt", "desc"))
                .thenReturn(PagedResponse.<ProductResponse>builder().content(java.util.List.of()).build());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_asAdmin_returns201() throws Exception {
        ProductRequest request = new ProductRequest(
                "Widget", "desc", BigDecimal.valueOf(9.99), null, "Acme", "cat-1", null, true);
        when(productService.createProduct(any())).thenReturn(ProductResponse.builder().id("p1").build());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void createProduct_invalidPrice_returns400() throws Exception {
        ProductRequest request = new ProductRequest(
                "Widget", "desc", BigDecimal.valueOf(-5), null, "Acme", "cat-1", null, true);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
