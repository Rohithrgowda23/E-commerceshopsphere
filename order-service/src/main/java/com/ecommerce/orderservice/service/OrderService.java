package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.request.CreateOrderRequest;
import com.ecommerce.orderservice.dto.response.OrderResponse;
import com.ecommerce.orderservice.dto.response.PagedResponse;

public interface OrderService {

    OrderResponse createOrder(String userId, CreateOrderRequest request);

    OrderResponse getOrder(String orderId);

    PagedResponse<OrderResponse> getOrdersForUser(String userId, int page, int size);

    OrderResponse cancelOrder(String orderId);
}
