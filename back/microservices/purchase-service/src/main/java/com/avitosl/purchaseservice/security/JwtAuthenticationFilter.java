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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
                        Set<String> roles = extractRolesFromToken(token);
                        List<GrantedAuthority> authorities = roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                            .collect(Collectors.toList());
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(user.getId(), null, authorities);
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
     * Extracts roles from a JWT token without verifying the signature.
     * For development only.
     */
    private Set<String> extractRolesFromToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length < 2) return new HashSet<>();
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
            Set<String> roles = new HashSet<>();

            // Extract realm_access.roles
            JsonNode realmAccess = node.get("realm_access");
            if (realmAccess != null && realmAccess.has("roles")) {
                for (JsonNode role : realmAccess.get("roles")) {
                    roles.add(role.asText());
                }
            }

            // Extract resource_access.*.roles
            JsonNode resourceAccess = node.get("resource_access");
            if (resourceAccess != null && resourceAccess.isObject()) {
                Iterator<String> fieldNames = resourceAccess.fieldNames();
                while (fieldNames.hasNext()) {
                    String client = fieldNames.next();
                    JsonNode clientNode = resourceAccess.get(client);
                    if (clientNode != null && clientNode.has("roles")) {
                        for (JsonNode role : clientNode.get("roles")) {
                            roles.add(role.asText());
                        }
                    }
                }
            }

            // Extract groups
            JsonNode groups = node.get("groups");
            if (groups != null && groups.isArray()) {
                for (JsonNode group : groups) {
                    roles.add(group.asText());
                }
            }

            return roles;
        } catch (Exception e) {
            return new HashSet<>();
        }
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
