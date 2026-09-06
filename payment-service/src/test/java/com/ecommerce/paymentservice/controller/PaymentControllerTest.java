package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.dto.request.CreatePaymentRequest;
import com.ecommerce.paymentservice.dto.response.PaymentResponse;
import com.ecommerce.paymentservice.security.JwtAuthenticationFilter;
import com.ecommerce.paymentservice.security.SecurityUtils;
import com.ecommerce.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void processPayment_validRequest_returns201() throws Exception {
        CreatePaymentRequest request = new CreatePaymentRequest("order-1", BigDecimal.valueOf(50), "CARD");
        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(paymentService.processPayment(any(), any())).thenReturn(
                PaymentResponse.builder().orderId("order-1").status("SUCCESS").build());

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void processPayment_missingAmount_returns400() throws Exception {
        CreatePaymentRequest request = new CreatePaymentRequest("order-1", null, "CARD");

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPayment_returns200() throws Exception {
        when(paymentService.getPaymentByOrderId("order-1")).thenReturn(
                PaymentResponse.builder().orderId("order-1").status("SUCCESS").build());

        mockMvc.perform(get("/api/payments/order-1"))
                .andExpect(status().isOk());
    }
}
