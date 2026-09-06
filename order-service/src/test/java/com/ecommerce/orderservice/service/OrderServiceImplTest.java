package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.CartClient;
import com.ecommerce.orderservice.client.CartClientResponse;
import com.ecommerce.orderservice.client.InventoryClient;
import com.ecommerce.orderservice.client.InventoryClientDtos;
import com.ecommerce.orderservice.client.UserClient;
import com.ecommerce.orderservice.client.UserClientResponse;
import com.ecommerce.orderservice.dto.request.CreateOrderRequest;
import com.ecommerce.orderservice.dto.response.OrderResponse;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.exception.BadRequestException;
import com.ecommerce.orderservice.exception.InsufficientStockException;
import com.ecommerce.orderservice.exception.InvalidOrderStateException;
import com.ecommerce.orderservice.kafka.producer.OrderEventProducer;
import com.ecommerce.orderservice.mapper.OrderMapper;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.security.SecurityUtils;
import com.ecommerce.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartClient cartClient;
    @Mock
    private UserClient userClient;
    @Mock
    private InventoryClient inventoryClient;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderEventProducer orderEventProducer;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private OrderServiceImpl orderService;

    private CreateOrderRequest sampleRequest() {
        return new CreateOrderRequest(
                "Jane Doe", "+15551234567", "123 Main St", null,
                "Springfield", "IL", "62704", "USA");
    }

    @Test
    void createOrder_emptyCart_throwsBadRequest() {
        when(userClient.getUser("user-1")).thenReturn(new UserClientResponse("user-1", "jane@x.com", "Jane", "Doe"));
        when(cartClient.getCart()).thenReturn(new CartClientResponse("user-1", List.of(), BigDecimal.ZERO));

        assertThrows(BadRequestException.class, () -> orderService.createOrder("user-1", sampleRequest()));
    }

    @Test
    void createOrder_unavailableItem_throwsBadRequest() {
        CartClientResponse.Item item = new CartClientResponse.Item(
                "p1", "Widget", "img", BigDecimal.TEN, 1, BigDecimal.TEN, false);
        when(userClient.getUser("user-1")).thenReturn(new UserClientResponse("user-1", "jane@x.com", "Jane", "Doe"));
        when(cartClient.getCart()).thenReturn(new CartClientResponse("user-1", List.of(item), BigDecimal.TEN));

        assertThrows(BadRequestException.class, () -> orderService.createOrder("user-1", sampleRequest()));
    }

    @Test
    void createOrder_insufficientStock_throwsInsufficientStock() {
        CartClientResponse.Item item = new CartClientResponse.Item(
                "p1", "Widget", "img", BigDecimal.TEN, 5, BigDecimal.valueOf(50), true);
        when(userClient.getUser("user-1")).thenReturn(new UserClientResponse("user-1", "jane@x.com", "Jane", "Doe"));
        when(cartClient.getCart()).thenReturn(new CartClientResponse("user-1", List.of(item), BigDecimal.valueOf(50)));

        InventoryClientDtos.StockCheckResponse.ItemAvailability unavailable =
                new InventoryClientDtos.StockCheckResponse.ItemAvailability("p1", 5, 2, false);
        when(inventoryClient.checkStock(any())).thenReturn(
                new InventoryClientDtos.StockCheckResponse(false, List.of(unavailable)));

        assertThrows(InsufficientStockException.class, () -> orderService.createOrder("user-1", sampleRequest()));
    }

    @Test
    void createOrder_validCheckout_createsOrderAndClearsCart() {
        CartClientResponse.Item item = new CartClientResponse.Item(
                "p1", "Widget", "img", BigDecimal.TEN, 2, BigDecimal.valueOf(20), true);
        when(userClient.getUser("user-1")).thenReturn(new UserClientResponse("user-1", "jane@x.com", "Jane", "Doe"));
        when(cartClient.getCart()).thenReturn(new CartClientResponse("user-1", List.of(item), BigDecimal.valueOf(20)));

        InventoryClientDtos.StockCheckResponse.ItemAvailability available =
                new InventoryClientDtos.StockCheckResponse.ItemAvailability("p1", 2, 10, true);
        when(inventoryClient.checkStock(any())).thenReturn(
                new InventoryClientDtos.StockCheckResponse(true, List.of(available)));

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(
                OrderResponse.builder().id("order-1").userId("user-1").build());

        OrderResponse response = orderService.createOrder("user-1", sampleRequest());

        assertEquals("order-1", response.getId());
        verify(inventoryClient).reserveStock(any());
        verify(cartClient).clearCart();
        verify(orderEventProducer).publishOrderCreated(any());
    }

    @Test
    void cancelOrder_nonCancellableStatus_throwsInvalidOrderState() {
        Order order = Order.builder().id("order-1").userId("user-1")
                .orderStatus(OrderStatus.DELIVERED).items(List.of()).build();

        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStateException.class, () -> orderService.cancelOrder("order-1"));
    }

    @Test
    void cancelOrder_pendingOrder_cancelsAndReleasesStock() {
        Order order = Order.builder().id("order-1").userId("user-1")
                .orderStatus(OrderStatus.PENDING).items(List.of()).build();

        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(
                OrderResponse.builder().id("order-1").orderStatus("CANCELLED").build());

        OrderResponse response = orderService.cancelOrder("order-1");

        assertEquals("CANCELLED", response.getOrderStatus());
        verify(inventoryClient).releaseStock(any());
        verify(orderEventProducer).publishOrderCancelled(any());
    }
}
