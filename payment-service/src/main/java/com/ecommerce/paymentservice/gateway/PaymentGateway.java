package com.ecommerce.paymentservice.gateway;

import java.math.BigDecimal;

/**
 * Abstraction over "wherever money actually moves." PaymentServiceImpl
 * only depends on this interface, never on a concrete gateway — so
 * swapping the mock implementation for a real Stripe/Razorpay adapter
 * later is a one-class change (implement this interface, wire it up as
 * the @Primary bean) with zero changes to the service layer, controller,
 * or Kafka event contracts.
 */
public interface PaymentGateway {

    PaymentGatewayResult charge(String orderId, BigDecimal amount, String paymentMethod);

    PaymentGatewayResult refund(String transactionRef, BigDecimal amount);
}
