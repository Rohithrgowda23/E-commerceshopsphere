package com.ecommerce.apigateway.security;

/**
 * Thrown when a JWT presented to the gateway is missing, malformed,
 * expired, or fails signature verification.
 */
public class InvalidJwtException extends RuntimeException {

    public InvalidJwtException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidJwtException(String message) {
        super(message);
    }
}
