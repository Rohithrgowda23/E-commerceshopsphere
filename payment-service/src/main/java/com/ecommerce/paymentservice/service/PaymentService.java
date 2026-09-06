package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.dto.request.CreatePaymentRequest;
import com.ecommerce.paymentservice.dto.response.PaymentResponse;

public interface PaymentService {

    /**
     * Used both by the REST endpoint (manual/explicit charge) and by the
     * Kafka OrderEventConsumer (automatic charge on ORDER_CREATED) — same
     * idempotent method, same orderId-based dedup.
     */
    PaymentResponse processPayment(String userId, CreatePaymentRequest request);

    PaymentResponse getPaymentByOrderId(String orderId);

    PaymentResponse refundPayment(String orderId);
}
