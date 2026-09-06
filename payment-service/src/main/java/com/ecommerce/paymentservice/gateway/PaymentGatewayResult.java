package com.ecommerce.paymentservice.gateway;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewayResult {

    private boolean success;
    private String transactionRef;
    private String failureReason;

    public static PaymentGatewayResult success(String transactionRef) {
        return new PaymentGatewayResult(true, transactionRef, null);
    }

    public static PaymentGatewayResult failure(String reason) {
        return new PaymentGatewayResult(false, null, reason);
    }
}
