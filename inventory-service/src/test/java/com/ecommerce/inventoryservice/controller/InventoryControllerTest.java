package com.ecommerce.inventoryservice.controller;

import com.ecommerce.inventoryservice.dto.request.StockCheckRequest;
import com.ecommerce.inventoryservice.dto.response.InventoryResponse;
import com.ecommerce.inventoryservice.dto.response.StockCheckResponse;
import com.ecommerce.inventoryservice.security.JwtAuthenticationFilter;
import com.ecommerce.inventoryservice.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.util.List;

import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getInventory_existingProduct_returns200() throws Exception {
        when(inventoryService.getInventory("p1"))
                .thenReturn(InventoryResponse.builder().productId("p1").availableQuantity(10).build());

        mockMvc.perform(get("/api/inventory/p1"))
                .andExpect(status().isOk());
    }

    @Test
    void checkStock_validRequest_returns200() throws Exception {
        StockCheckRequest request = new StockCheckRequest(
                List.of(new StockCheckRequest.StockItem("p1", 2)));
        when(inventoryService.checkStock(any())).thenReturn(
                StockCheckResponse.builder().allInStock(true).items(List.of()).build());

        mockMvc.perform(post("/api/inventory/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void checkStock_emptyItems_returns400() throws Exception {
        StockCheckRequest request = new StockCheckRequest(List.of());

        mockMvc.perform(post("/api/inventory/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
