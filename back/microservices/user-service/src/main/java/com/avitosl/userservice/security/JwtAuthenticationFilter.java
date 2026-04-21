package com.avitosl.userservice.security;

import com.avitosl.userservice.entity.User;
import com.avitosl.userservice.service.AuthService;
import com.avitosl.userservice.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        // Skip JWT validation for auth endpoints
        if (path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = getJwtFromRequest(request);

        if (StringUtils.hasText(token)) {
            // Try internal JWT first
            if (authService.validateToken(token)) {
                String username = authService.getUsernameFromToken(token);
                Long userId = authService.getUserIdFromToken(token);
                setAuthentication(request, username, userId);
            } else {
                // Try Keycloak token: extract 'sub' without signature verification (dev)
                try {
                    String keycloakId = extractSubFromToken(token);
                    if (keycloakId != null) {
                        User user = userService.getUserByKeycloakId(keycloakId);
                        setAuthentication(request, user.getUsername(), user.getId());
                    }
                } catch (Exception e) {
                    // Invalid Keycloak token, continue unauthenticated
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(HttpServletRequest request, String username, Long userId) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, null);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
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
        if (parts.length < 2) return null;
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
