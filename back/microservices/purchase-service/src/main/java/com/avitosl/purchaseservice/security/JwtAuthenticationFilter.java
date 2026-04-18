package com.avitosl.purchaseservice.security;

import com.avitosl.purchaseservice.feign.UserServiceClient;
import com.avitosl.purchaseservice.response.UserResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserServiceClient userServiceClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        // Skip JWT validation for actuator and swagger endpoints
        if (path.startsWith("/actuator") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = getJwtFromRequest(request);

        if (StringUtils.hasText(token)) {
            try {
                // Extract 'sub' (Keycloak user ID) from token without signature verification (dev only)
                String keycloakId = extractSubFromToken(token);
                if (keycloakId != null) {
                    // Fetch internal user by keycloakId
                    UserResponse user = userServiceClient.getUserByKeycloakId(keycloakId);
                    if (user != null && user.getId() != null) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(user.getId(), null, Collections.emptyList());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        request.setAttribute("jwtToken", token);
                    }
                }
            } catch (Exception e) {
                // Log and continue unauthenticated
                System.err.println("JWT authentication failed: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return "";
    }

    /**
     * Extracts the 'sub' claim from a JWT token without verifying the signature.
     * For development only.
     */
    private String extractSubFromToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length < 2) return null; // not a JWT
        String payload = parts[1];
        // Fix base64 padding
        String padded = payload.replace('-', '+').replace('_', '/');
        switch (padded.length() % 4) {
            case 2: padded += "=="; break;
            case 3: padded += "="; break;
            default: break;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(padded);
            JsonNode node = objectMapper.readTree(decoded);
            JsonNode subNode = node.get("sub");
            return subNode != null ? subNode.asText() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
