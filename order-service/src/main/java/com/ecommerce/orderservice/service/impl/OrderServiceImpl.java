package com.ecommerce.orderservice.service.impl;

import com.ecommerce.orderservice.client.CartClient;
import com.ecommerce.orderservice.client.CartClientResponse;
import com.ecommerce.orderservice.client.InventoryClient;
import com.ecommerce.orderservice.client.InventoryClientDtos;
import com.ecommerce.orderservice.client.UserClient;
import com.ecommerce.orderservice.dto.request.CreateOrderRequest;
import com.ecommerce.orderservice.dto.response.OrderResponse;
import com.ecommerce.orderservice.dto.response.PagedResponse;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderItem;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.exception.BadRequestException;
import com.ecommerce.orderservice.exception.InsufficientStockException;
import com.ecommerce.orderservice.exception.InvalidOrderStateException;
import com.ecommerce.orderservice.exception.ResourceNotFoundException;
import com.ecommerce.orderservice.kafka.producer.OrderEventProducer;
import com.ecommerce.orderservice.mapper.OrderMapper;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.security.SecurityUtils;
import com.ecommerce.orderservice.service.OrderService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    private static final Set<OrderStatus> CANCELLABLE_STATUSES =
            Set.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PROCESSING);

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final UserClient userClient;
    private final InventoryClient inventoryClient;
    private final OrderMapper orderMapper;
    private final OrderEventProducer orderEventProducer;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public OrderResponse createOrder(String userId, CreateOrderRequest request) {
        // 1. Validate the user exists (defense in depth beyond the JWT itself).
        userClient.getUser(userId);

        // 2. Pull the live cart — order items are never taken from the request body.
        CartClientResponse cart = cartClient.getCart();
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot place an order with an empty cart");
        }

        List<String> unavailable = cart.getItems().stream()
                .filter(item -> !item.isAvailable())
                .map(CartClientResponse.Item::getProductId)
                .collect(Collectors.toList());
        if (!unavailable.isEmpty()) {
            throw new BadRequestException("Cart contains unavailable products: " + unavailable);
        }

        // 3. Check stock before committing to an order.
        List<InventoryClientDtos.StockItem> stockItems = cart.getItems().stream()
                .map(item -> new InventoryClientDtos.StockItem(item.getProductId(), item.getQuantity()))
                .collect(Collectors.toList());

        InventoryClientDtos.StockCheckResponse stockCheck =
                inventoryClient.checkStock(new InventoryClientDtos.StockCheckRequest(stockItems));
        if (!stockCheck.isAllInStock()) {
            List<String> outOfStock = stockCheck.getItems().stream()
                    .filter(i -> !i.isInStock())
                    .map(InventoryClientDtos.StockCheckResponse.ItemAvailability::getProductId)
                    .collect(Collectors.toList());
            throw new InsufficientStockException("Insufficient stock for products: " + outOfStock);
        }

        // 4. Build and save the order, snapshotting product name/price from the cart.
        Order order = Order.builder()
                .userId(userId)
                .totalAmount(cart.getTotal())
                .shippingRecipientName(request.getShippingRecipientName())
                .shippingPhoneNumber(request.getShippingPhoneNumber())
                .shippingAddressLine1(request.getShippingAddressLine1())
                .shippingAddressLine2(request.getShippingAddressLine2())
                .shippingCity(request.getShippingCity())
                .shippingState(request.getShippingState())
                .shippingPostalCode(request.getShippingPostalCode())
                .shippingCountry(request.getShippingCountry())
                .orderStatus(OrderStatus.PENDING)
                .build();

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(item -> OrderItem.builder()
                        .order(order)
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);
        log.info("Created order id={} for userId={} totalAmount={}",
                savedOrder.getId(), userId, savedOrder.getTotalAmount());

        // 5. Reserve stock synchronously so checkout fails fast on a race
        // (stock consumed by another order between the check above and now).
        try {
            List<InventoryClientDtos.StockItem> reserveItems = orderItems.stream()
                    .map(i -> new InventoryClientDtos.StockItem(i.getProductId(), i.getQuantity()))
                    .collect(Collectors.toList());
            inventoryClient.reserveStock(new InventoryClientDtos.ReserveStockRequest(savedOrder.getId(), reserveItems));
        } catch (FeignException.Conflict e) {
            savedOrder.setOrderStatus(OrderStatus.CANCELLED);
            orderRepository.save(savedOrder);
            log.warn("Reservation failed for orderId={}, order cancelled: {}", savedOrder.getId(), e.getMessage());
            throw new InsufficientStockException("Stock became unavailable while placing the order");
        }

        // 6. Checkout succeeded — clear the cart and notify the rest of the platform.
        cartClient.clearCart();
        orderEventProducer.publishOrderCreated(savedOrder);

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderId) {
        Order order = findOrThrow(orderId);
        securityUtils.assertOwnerOrAdminForRead(order.getUserId());
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getOrdersForUser(String userId, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);

        Page<Order> orders = orderRepository.findByUserId(
                userId, PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")));

        return PagedResponse.from(orders.map(orderMapper::toResponse));
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(String orderId) {
        Order order = findOrThrow(orderId);
        securityUtils.assertOwnerOrAdminForWrite(order.getUserId());

        if (!CANCELLABLE_STATUSES.contains(order.getOrderStatus())) {
            throw new InvalidOrderStateException(
                    "Order in status " + order.getOrderStatus() + " cannot be cancelled");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);

        List<InventoryClientDtos.StockItem> items = order.getItems().stream()
                .map(i -> new InventoryClientDtos.StockItem(i.getProductId(), i.getQuantity()))
                .collect(Collectors.toList());
        inventoryClient.releaseStock(new InventoryClientDtos.ReleaseStockRequest(orderId, items));

        orderEventProducer.publishOrderCancelled(saved);
        log.info("Cancelled orderId={}", orderId);

        return orderMapper.toResponse(saved);
    }

    private Order findOrThrow(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No order found with id: " + orderId));
    }
}
