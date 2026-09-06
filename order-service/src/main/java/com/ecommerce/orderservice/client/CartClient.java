package com.ecommerce.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "cart-service", configuration = com.ecommerce.orderservice.config.FeignConfig.class)
public interface CartClient {

    @GetMapping("/api/cart")
    CartClientResponse getCart();

    @DeleteMapping("/api/cart")
    void clearCart();
}
