package com.ecommerce.paymentservice.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulates a payment provider for local development and demos: succeeds
 * most of the time, fails a configurable percentage of the time (to
 * exercise the PAYMENT_FAILED path end-to-end), and adds a small
 * artificial delay so the async flow is visibly asynchronous rather than
 * instantaneous.
 */
@Component
public class MockPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentGateway.class);

    private final int failureRatePercent;
    private final long simulatedLatencyMs;

    public MockPaymentGateway(
            @Value("${payment.mock.failure-rate-percent:10}") int failureRatePercent,
            @Value("${payment.mock.simulated-latency-ms:300}") long simulatedLatencyMs) {
        this.failureRatePercent = failureRatePercent;
        this.simulatedLatencyMs = simulatedLatencyMs;
    }

    @Override
    public PaymentGatewayResult charge(String orderId, BigDecimal amount, String paymentMethod) {
        simulateLatency();

        boolean shouldFail = ThreadLocalRandom.current().nextInt(100) < failureRatePercent;
        if (shouldFail) {
            log.info("Mock gateway declining charge for orderId={} amount={}", orderId, amount);
            return PaymentGatewayResult.failure("Card declined by issuing bank (simulated)");
        }

        String transactionRef = "mock_txn_" + UUID.randomUUID();
        log.info("Mock gateway approved charge for orderId={} amount={} ref={}", orderId, amount, transactionRef);
        return PaymentGatewayResult.success(transactionRef);
    }

    @Override
    public PaymentGatewayResult refund(String transactionRef, BigDecimal amount) {
        simulateLatency();
        String refundRef = "mock_refund_" + UUID.randomUUID();
        log.info("Mock gateway refunded transactionRef={} amount={} refundRef={}", transactionRef, amount, refundRef);
        return PaymentGatewayResult.success(refundRef);
    }

    private void simulateLatency() {
        try {
            Thread.sleep(simulatedLatencyMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
