package com.ecommerce.paymentservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String id;
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private String transactionRef;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
