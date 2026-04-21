package com.avitosl.postservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

     public String getUserKeycloakIdFromToken(String token) {
         try {
             // Remove Bearer prefix if present
             if (token != null && token.startsWith("Bearer ")) {
                 token = token.substring(7);
             }
             // Decode payload without verification (token already verified by API Gateway/Resource Server)
             String[] parts = token.split("\\.");
             if (parts.length != 3) {
                 throw new RuntimeException("Invalid JWT token");
             }
             // Decode payload (second part)
             String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
             // Parse JSON
             com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
             java.util.Map<String, Object> claims = mapper.readValue(payload, java.util.Map.class);
             return (String) claims.get("sub");
         } catch (Exception e) {
             throw new RuntimeException("Invalid JWT token", e);
         }
     }

     public String getClaimFromToken(String token, String claimName) {
         try {
             if (token != null && token.startsWith("Bearer ")) {
                 token = token.substring(7);
             }
             String[] parts = token.split("\\.");
             if (parts.length != 3) {
                 throw new RuntimeException("Invalid JWT token");
             }
             String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
             com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
             java.util.Map<String, Object> claims = mapper.readValue(payload, java.util.Map.class);
             Object value = claims.get(claimName);
             return value != null ? value.toString() : null;
         } catch (Exception e) {
             throw new RuntimeException("Invalid JWT token", e);
         }
     }
}
