package com.avitosl.chatservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    public String getUserKeycloakIdFromToken(String token) {
        try {
            // Decode without verification since token is already verified by Keycloak Resource Server
            String[] parts = token.replace("Bearer ", "").split("\\.");
            if (parts.length != 3) {
                throw new RuntimeException("Invalid JWT token");
            }
            // Decode payload
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            // Parse as JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, Object> claims = mapper.readValue(payload, java.util.Map.class);
            return (String) claims.get("sub");
        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }
}