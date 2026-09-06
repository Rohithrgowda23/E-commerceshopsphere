package com.ecommerce.orderservice.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * order-service's Feign clients (cart-service, user-service,
 * inventory-service) act on behalf of whichever user is checking out, so
 * every outgoing call must carry that user's original JWT rather than a
 * service-account token — cart-service and user-service both derive
 * "who is calling" purely from the Authorization header.
 */
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor authorizationHeaderForwardingInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null) {
                    requestTemplate.header("Authorization", authHeader);
                }
            }
        };
    }
}
