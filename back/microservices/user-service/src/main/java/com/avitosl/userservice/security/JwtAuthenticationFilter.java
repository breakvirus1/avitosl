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
                        Set<String> roles = extractRolesFromToken(token);
                        List<GrantedAuthority> authorities = roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                            .collect(Collectors.toList());
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        request.setAttribute("userId", user.getId());
                        request.setAttribute("username", user.getUsername());
                    }
                } catch (Exception e) {
                    // Invalid Keycloak token, continue unauthenticated
                }
            }
        }

        filterChain.doFilter(request, response);
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
