package com.ecommerce.paymentservice.service;

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
import com.ecommerce.paymentservice.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentGateway paymentGateway;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private PaymentEventProducer paymentEventProducer;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void processPayment_gatewaySucceeds_marksSuccessAndPublishesEvent() {
        CreatePaymentRequest request = new CreatePaymentRequest("order-1", BigDecimal.valueOf(100), "CARD");

        when(paymentRepository.findByOrderId("order-1")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentGateway.charge(anyString(), any(), anyString()))
                .thenReturn(PaymentGatewayResult.success("txn-123"));
        when(paymentMapper.toResponse(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            return PaymentResponse.builder().orderId(p.getOrderId()).status(p.getStatus().name()).build();
        });

        PaymentResponse response = paymentService.processPayment("user-1", request);

        assertEquals("SUCCESS", response.getStatus());
        verify(paymentEventProducer).publishPaymentSuccess(eq("order-1"), any());
    }

    @Test
    void processPayment_gatewayFails_marksFailedAndPublishesEvent() {
        CreatePaymentRequest request = new CreatePaymentRequest("order-2", BigDecimal.valueOf(50), "CARD");

        when(paymentRepository.findByOrderId("order-2")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentGateway.charge(anyString(), any(), anyString()))
                .thenReturn(PaymentGatewayResult.failure("Card declined"));
        when(paymentMapper.toResponse(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            return PaymentResponse.builder().orderId(p.getOrderId()).status(p.getStatus().name()).build();
        });

        PaymentResponse response = paymentService.processPayment("user-1", request);

        assertEquals("FAILED", response.getStatus());
        verify(paymentEventProducer).publishPaymentFailed(eq("order-2"), any(), anyString());
    }

    @Test
    void processPayment_duplicateOrderId_returnsExistingWithoutRecharging() {
        CreatePaymentRequest request = new CreatePaymentRequest("order-3", BigDecimal.valueOf(75), "CARD");
        Payment existing = Payment.builder().orderId("order-3").status(PaymentStatus.SUCCESS).build();

        when(paymentRepository.findByOrderId("order-3")).thenReturn(Optional.of(existing));
        when(paymentMapper.toResponse(existing)).thenReturn(
                PaymentResponse.builder().orderId("order-3").status("SUCCESS").build());

        PaymentResponse response = paymentService.processPayment("user-1", request);

        assertEquals("SUCCESS", response.getStatus());
        verify(paymentGateway, never()).charge(anyString(), any(), anyString());
    }

    @Test
    void refundPayment_nonSuccessfulPayment_throwsInvalidPaymentState() {
        Payment payment = Payment.builder().orderId("order-4").status(PaymentStatus.FAILED).build();
        when(paymentRepository.findByOrderId("order-4")).thenReturn(Optional.of(payment));

        assertThrows(InvalidPaymentStateException.class, () -> paymentService.refundPayment("order-4"));
    }

    @Test
    void getPaymentByOrderId_unknownOrder_throwsResourceNotFound() {
        when(paymentRepository.findByOrderId("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.getPaymentByOrderId("missing"));
    }
}
