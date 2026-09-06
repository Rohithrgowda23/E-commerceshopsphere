package com.ecommerce.cartservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Synchronous call to product-service (via Eureka's "product-service" name)
 * to fetch the live price/availability/name for a product when rendering
 * the cart. This is a case where an immediate answer is required — Kafka
 * would mean the cart could show stale prices — so OpenFeign/REST is used
 * instead of an event.
 */
@FeignClient(name = "product-service", fallbackFactory = ProductClientFallbackFactory.class)
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductClientResponse getProduct(@PathVariable("id") String id);
}
