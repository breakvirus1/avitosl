package com.avitosl.chatservice.util;

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
    void getUserKeycloakIdFromToken_ShouldExtractSub_WhenValidToken() throws Exception {
        // Given
        String token = createToken("user-123");

        // When
        String result = jwtUtil.getUserKeycloakIdFromToken(token);

        // Then
        assertEquals("user-123", result);
    }

    @Test
    void getUserKeycloakIdFromToken_ShouldRemoveBearerPrefix() throws Exception {
        // Given
        String token = "Bearer " + createToken("user-456");

        // When
        String result = jwtUtil.getUserKeycloakIdFromToken(token);

        // Then
        assertEquals("user-456", result);
    }

    @Test
    void getUserKeycloakIdFromToken_ShouldThrowException_WhenTokenInvalid() {
        // Given
        String invalidToken = "not-a-jwt-token";

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            jwtUtil.getUserKeycloakIdFromToken(invalidToken);
        });
        assertThat(exception.getMessage()).isEqualTo("Invalid JWT token");
    }

    private String createToken(String sub) throws Exception {
        String header = "{\"alg\":\"none\"}";
        String payload = "{\"sub\":\"" + sub + "\"}";
        java.util.Base64.Encoder encoder = java.util.Base64.getUrlEncoder().withoutPadding();
        String encodedHeader = encoder.encodeToString(header.getBytes());
        String encodedPayload = encoder.encodeToString(payload.getBytes());
        return encodedHeader + "." + encodedPayload + ".signature";
    }
}
