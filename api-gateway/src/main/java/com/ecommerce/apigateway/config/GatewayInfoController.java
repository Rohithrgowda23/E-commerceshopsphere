package com.ecommerce.apigateway.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Lightweight endpoint the frontend (or a load balancer) can use to
 * confirm the gateway itself is up, independent of Actuator.
 */
@RestController
public class GatewayInfoController {

    @GetMapping("/api/gateway/status")
    public Mono<Map<String, Object>> status() {
        return Mono.just(Map.of(
                "service", "api-gateway",
                "status", "UP"
        ));
    }
}
