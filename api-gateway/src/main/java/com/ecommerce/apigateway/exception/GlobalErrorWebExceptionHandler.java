package com.ecommerce.apigateway.exception;

import com.ecommerce.apigateway.security.InvalidJwtException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ensures every error surfaced by the gateway (unmatched route, downstream
 * timeout, invalid JWT, etc.) comes back as a consistent JSON payload
 * instead of a default Whitelabel HTML page.
 *
 * Builds the response body directly from the caught exception rather than
 * going through Spring Boot's ErrorAttributes machinery — that API expects
 * the exception to already be attached to the exchange via a specific
 * attribute (normally done by the default error-handling filter chain,
 * which this class replaces), and calling it without that step throws
 * "Missing exception attribute in ServerWebExchange". Since the exception
 * is already available as a method parameter here, there's no need for
 * that indirection at all.
 */
@Component
@Order(-2)
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = resolveStatus(ex);
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", resolveMessage(ex, status));
        body.put("path", exchange.getRequest().getURI().getPath());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (Exception serializationError) {
            bytes = ("{\"error\":\"" + status.getReasonPhrase() + "\"}").getBytes();
        }

        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    private HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof InvalidJwtException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (ex instanceof NoResourceFoundException) {
            // No gateway route matched this path at all — a routing/config
            // gap, not a downstream failure. 404 is the honest status.
            return HttpStatus.NOT_FOUND;
        }
        if (ex instanceof org.springframework.web.server.ResponseStatusException rse) {
            return HttpStatus.valueOf(rse.getStatusCode().value());
        }
        if (ex instanceof java.util.concurrent.TimeoutException) {
            return HttpStatus.GATEWAY_TIMEOUT;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveMessage(Throwable ex, HttpStatus status) {
        if (status == HttpStatus.NOT_FOUND) {
            return "No route matched for this path";
        }
        return ex.getMessage() != null ? ex.getMessage() : status.getReasonPhrase();
    }
}