package com.ecommerce.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {

    private static final String SECRET = "test-secret-key-for-unit-tests-only-not-for-production-use";

    private JwtUtil jwtUtil;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET);
        signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void parseAndValidate_validToken_returnsClaims() {
        String token = Jwts.builder()
                .subject("user-123")
                .claim("email", "test@example.com")
                .claim("roles", List.of("USER"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey)
                .compact();

        Claims claims = jwtUtil.parseAndValidate(token);

        assertEquals("user-123", jwtUtil.extractUserId(claims));
        assertEquals("test@example.com", jwtUtil.extractEmail(claims));
        assertEquals(List.of("USER"), jwtUtil.extractRoles(claims));
    }

    @Test
    void parseAndValidate_expiredToken_throwsInvalidJwtException() {
        String token = Jwts.builder()
                .subject("user-123")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(signingKey)
                .compact();

        assertThrows(InvalidJwtException.class, () -> jwtUtil.parseAndValidate(token));
    }

    @Test
    void parseAndValidate_malformedToken_throwsInvalidJwtException() {
        assertThrows(InvalidJwtException.class, () -> jwtUtil.parseAndValidate("not-a-real-jwt"));
    }

    @Test
    void parseAndValidate_wrongSignature_throwsInvalidJwtException() {
        SecretKey otherKey = Keys.hmacShaKeyFor(
                "a-completely-different-secret-key-used-to-sign-wrong".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("user-123")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(otherKey)
                .compact();

        assertThrows(InvalidJwtException.class, () -> jwtUtil.parseAndValidate(token));
    }
}
