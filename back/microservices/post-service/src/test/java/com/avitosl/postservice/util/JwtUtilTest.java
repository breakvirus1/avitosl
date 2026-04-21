package com.avitosl.postservice.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void getUserKeycloakIdFromToken_ShouldExtractSubClaim_WhenTokenIsValid() throws Exception {
        // Given - create a simple JWT with sub claim
        String token = createTestToken("test-sub-123");

        // When
        String result = jwtUtil.getUserKeycloakIdFromToken(token);

        // Then
        assertEquals("test-sub-123", result);
    }

    @Test
    void getUserKeycloakIdFromToken_ShouldRemoveBearerPrefix_WhenPresent() throws Exception {
        // Given
        String tokenWithBearer = "Bearer " + createTestToken("user-456");

        // When
        String result = jwtUtil.getUserKeycloakIdFromToken(tokenWithBearer);

        // Then
        assertEquals("user-456", result);
    }

    @Test
    void getUserKeycloakIdFromToken_ShouldThrowException_WhenTokenIsMalformed() {
        // Given
        String invalidToken = "not-a-token";

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            jwtUtil.getUserKeycloakIdFromToken(invalidToken);
        });
        assertThat(exception.getMessage()).isEqualTo("Invalid JWT token");
    }

    @Test
    void getClaimFromToken_ShouldExtractCustomClaim_WhenPresent() throws Exception {
        // Given
        String token = createComplexToken("sub-claim", "custom-value");

        // When
        String result = jwtUtil.getClaimFromToken(token, "custom");

        // Then
        assertEquals("custom-value", result);
    }

    @Test
    void getClaimFromToken_ShouldReturnNull_WhenClaimNotPresent() throws Exception {
        // Given
        String token = createTestToken("test-sub");

        // When
        String result = jwtUtil.getClaimFromToken(token, "nonexistent");

        // Then
        assertNull(result);
    }

    @Test
    void getClaimFromToken_ShouldHandleBearerPrefix() throws Exception {
        // Given
        String token = "Bearer " + createTestToken("sub");

        // When
        String result = jwtUtil.getClaimFromToken(token, "sub");

        // Then
        assertEquals("sub", result);
    }

    private String createTestToken(String sub) throws Exception {
        // Create a minimal JWT token (header.payload.signature) with base64url encoding
        String header = "{\"alg\":\"none\"}";
        String payload = "{\"sub\":\"" + sub + "\"}";
        String signature = "dummy"; // non-empty to ensure 3 parts after split

        java.util.Base64.Encoder encoder = java.util.Base64.getUrlEncoder().withoutPadding();
        String encodedHeader = encoder.encodeToString(header.getBytes());
        String encodedPayload = encoder.encodeToString(payload.getBytes());
        String encodedSignature = encoder.encodeToString(signature.getBytes());

        return encodedHeader + "." + encodedPayload + "." + encodedSignature;
    }

    private String createComplexToken(String sub, String custom) throws Exception {
        String header = "{\"alg\":\"none\"}";
        String payload = "{\"sub\":\"" + sub + "\",\"custom\":\"" + custom + "\"}";
        String signature = "dummy";

        java.util.Base64.Encoder encoder = java.util.Base64.getUrlEncoder().withoutPadding();
        String encodedHeader = encoder.encodeToString(header.getBytes());
        String encodedPayload = encoder.encodeToString(payload.getBytes());
        String encodedSignature = encoder.encodeToString(signature.getBytes());

        return encodedHeader + "." + encodedPayload + "." + encodedSignature;
    }
}
