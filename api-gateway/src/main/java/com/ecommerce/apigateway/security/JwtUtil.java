package com.ecommerce.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Validates JWT signature and expiration at the gateway edge, and extracts
 * the claims needed for downstream routing/logging (userId, email, roles).
 *
 * The gateway performs a lightweight, stateless check here. Each downstream
 * microservice independently re-validates the token (defense in depth) and
 * enforces fine-grained, endpoint-level authorization with Spring Security.
 */
@Component
public class JwtUtil {

    private final SecretKey signingKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims parseAndValidate(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new InvalidJwtException("JWT token has expired", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtException("JWT token is invalid", e);
        }
    }

    public String extractUserId(Claims claims) {
        return claims.getSubject();
    }

    public String extractEmail(Claims claims) {
        return claims.get("email", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(Claims claims) {
        return claims.get("roles", List.class);
    }
}
