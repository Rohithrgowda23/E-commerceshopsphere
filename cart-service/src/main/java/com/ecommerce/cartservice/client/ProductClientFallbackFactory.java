package com.ecommerce.cartservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * If product-service is unreachable, the cart should still render (with
 * the item flagged unavailable/price unknown) rather than a 500 blocking
 * checkout entirely. Cart mutations (add/update) still fail explicitly —
 * see CartServiceImpl — this fallback only applies to enrichment on read.
 */
@Component
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {

    private static final Logger log = LoggerFactory.getLogger(ProductClientFallbackFactory.class);

    @Override
    public ProductClient create(Throwable cause) {
        return productId -> {
            log.warn("product-service call failed for productId={}: {}", productId, cause.getMessage());
            ProductClientResponse fallback = new ProductClientResponse();
            fallback.setId(productId);
            fallback.setName("Product unavailable");
            fallback.setAvailable(false);
            return fallback;
        };
    }
}
