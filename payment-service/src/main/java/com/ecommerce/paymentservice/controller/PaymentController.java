package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.dto.request.CreatePaymentRequest;
import com.ecommerce.paymentservice.dto.response.PaymentResponse;
import com.ecommerce.paymentservice.security.SecurityUtils;
import com.ecommerce.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Mock payment processing and refunds")
public class PaymentController {

    private final PaymentService paymentService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(summary = "Charge for an order (normally triggered automatically by ORDER_CREATED)")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(securityUtils.getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get the payment for an order")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    @PostMapping("/{orderId}/refund")
    @Operation(summary = "Refund a successful payment")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.refundPayment(orderId));
    }
}
