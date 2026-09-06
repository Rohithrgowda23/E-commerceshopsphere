package com.ecommerce.apigateway.filter;

import com.ecommerce.apigateway.config.SecurityProperties;
import com.ecommerce.apigateway.security.InvalidJwtException;
import com.ecommerce.apigateway.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * GatewayFilterFactory named "JwtAuthenticationFilter" (matches the
 * "filters: - name: JwtAuthenticationFilter" entries in application.yml).
 *
 * Responsibilities:
 *  - Skip validation for configured public endpoints (e.g. GET /api/products/**,
 *    POST /api/auth/login, POST /api/auth/register).
 *  - Reject requests with a missing/invalid/expired JWT with 401.
 *  - On success, forward the request with extra headers (X-User-Id,
 *    X-User-Email, X-User-Roles) so downstream services don't need to
 *    re-parse the token to know who's calling — they still independently
 *    re-validate the raw Authorization header themselves for authorization.
 */
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;
    private final SecurityProperties securityProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtUtil jwtUtil, SecurityProperties securityProperties) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.securityProperties = securityProperties;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (isPublicEndpoint(request)) {
                return chain.filter(exchange);
            }

            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorized(exchange, "Missing or malformed Authorization header");
            }

            String token = authHeader.substring(7);

            try {
                Claims claims = jwtUtil.parseAndValidate(token);
                String userId = jwtUtil.extractUserId(claims);
                String email = jwtUtil.extractEmail(claims);
                List<String> roles = jwtUtil.extractRoles(claims);

                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id", userId == null ? "" : userId)
                        .header("X-User-Email", email == null ? "" : email)
                        .header("X-User-Roles", roles == null ? "" : String.join(",", roles))
                        .build();

                ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
                return chain.filter(mutatedExchange);
            } catch (InvalidJwtException e) {
                return unauthorized(exchange, e.getMessage());
            }
        };
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // GET on the product catalog is always public; mutating verbs on the
        // same paths still require a valid JWT (and ADMIN role, enforced
        // downstream by product-service).
        boolean isCatalogRead = path.startsWith("/api/products") && HttpMethod.GET.equals(method);
        if (isCatalogRead) {
            return true;
        }

        return securityProperties.getPublicEndpoints().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String reason) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("X-Auth-Error", reason);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // No per-route configuration needed today; kept as an extension
        // point (e.g. per-route required roles) without breaking the
        // GatewayFilterFactory contract.
    }
}
