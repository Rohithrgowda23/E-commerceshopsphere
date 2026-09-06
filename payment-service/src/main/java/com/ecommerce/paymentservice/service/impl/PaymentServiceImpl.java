package com.ecommerce.paymentservice.service.impl;

import com.ecommerce.paymentservice.dto.request.CreatePaymentRequest;
import com.ecommerce.paymentservice.dto.response.PaymentResponse;
import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.entity.PaymentStatus;
import com.ecommerce.paymentservice.exception.InvalidPaymentStateException;
import com.ecommerce.paymentservice.exception.ResourceNotFoundException;
import com.ecommerce.paymentservice.gateway.PaymentGateway;
import com.ecommerce.paymentservice.gateway.PaymentGatewayResult;
import com.ecommerce.paymentservice.kafka.producer.PaymentEventProducer;
import com.ecommerce.paymentservice.mapper.PaymentMapper;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import com.ecommerce.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentMapper paymentMapper;
    private final PaymentEventProducer paymentEventProducer;

    @Override
    @Transactional
    public PaymentResponse processPayment(String userId, CreatePaymentRequest request) {
        // Idempotency: a redelivered ORDER_CREATED event (or a retried
        // REST call) for an order that already has a payment record
        // returns the existing result instead of charging twice.
        Payment existing = paymentRepository.findByOrderId(request.getOrderId()).orElse(null);
        if (existing != null) {
            log.info("Payment already exists for orderId={} (status={}), skipping duplicate charge",
                    request.getOrderId(), existing.getStatus());
            return paymentMapper.toResponse(existing);
        }

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(userId)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .build();
        payment = paymentRepository.save(payment);

        PaymentGatewayResult result = paymentGateway.charge(
                request.getOrderId(), request.getAmount(), request.getPaymentMethod());

        if (result.isSuccess()) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionRef(result.getTransactionRef());
            Payment saved = paymentRepository.save(payment);
            log.info("Payment succeeded for orderId={} paymentId={}", request.getOrderId(), saved.getId());
            paymentEventProducer.publishPaymentSuccess(request.getOrderId(), saved.getId());
            return paymentMapper.toResponse(saved);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(result.getFailureReason());
            Payment saved = paymentRepository.save(payment);
            log.info("Payment failed for orderId={} reason={}", request.getOrderId(), result.getFailureReason());
            paymentEventProducer.publishPaymentFailed(request.getOrderId(), saved.getId(), result.getFailureReason());
            return paymentMapper.toResponse(saved);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(String orderId) {
        Payment payment = findOrThrow(orderId);
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(String orderId) {
        Payment payment = findOrThrow(orderId);

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidPaymentStateException(
                    "Only a successful payment can be refunded; current status: " + payment.getStatus());
        }

        PaymentGatewayResult result = paymentGateway.refund(payment.getTransactionRef(), payment.getAmount());

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setTransactionRef(result.getTransactionRef());
        Payment saved = paymentRepository.save(payment);

        log.info("Refunded payment for orderId={} paymentId={}", orderId, saved.getId());
        return paymentMapper.toResponse(saved);
    }

    private Payment findOrThrow(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No payment found for orderId: " + orderId));
    }
}
