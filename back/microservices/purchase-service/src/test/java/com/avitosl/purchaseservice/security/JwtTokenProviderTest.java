package com.avitosl.purchaseservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.jsonwebtoken.Jwts;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenTokenIsValid() {
        // Given
        String token = generateTestToken("testuser", 1L);

        // When
        boolean result = jwtTokenProvider.validateToken(token);

        // Then
        assertTrue(result);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsInvalid() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean result = jwtTokenProvider.validateToken(invalidToken);

        // Then
        assertFalse(result);
    }

    @Test
    void getUserIdFromToken_ShouldReturnUserId_WhenTokenIsValid() {
        // Given
        String token = generateTestToken("testuser", 42L);

        // When
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        // Then
        assertEquals(42L, userId);
    }

    @Test
    void getUsernameFromToken_ShouldReturnUsername_WhenTokenIsValid() {
        // Given
        String token = generateTestToken("john_doe", 1L);

        // When
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Then
        assertEquals("john_doe", username);
    }

    private String generateTestToken(String subject, Long userId) {
        return Jwts.builder()
                .setSubject(subject)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS256, "avito-secret-key-for-jwt-tokens-must-be-at-least-256-bits-long".getBytes())
                .compact();
    }
}
