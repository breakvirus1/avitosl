package com.example.avito.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class BearerTokenAuthenticator {

    private final JwtDecoder jwtDecoder;

    @Autowired
    public BearerTokenAuthenticator(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    public Authentication authenticate(String authHeaderValue) {
        if (authHeaderValue == null || !authHeaderValue.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeaderValue.substring(7);
        try {
            Jwt jwt = jwtDecoder.decode(token);
            
            // Extract authorities from JWT claims
            var authorities = jwt.getClaimAsStringList("authorities")
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
            
            // Also check realm_access.roles if present
            var realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess instanceof java.util.Map) {
                var roles = (java.util.Map<String, Object>) realmAccess;
                if (roles.containsKey("roles")) {
                    var rolesList = (java.util.List<String>) roles.get("roles");
                    for (String role : rolesList) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                }
            }

            // Check groups if present
            var groups = jwt.getClaimAsStringList("groups");
            if (groups != null) {
                for (String group : groups) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + group));
                }
            }

            String username = jwt.getSubject();
            if (username == null) {
                username = jwt.getClaimAsString("preferred_username");
            }

            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(jwt, null, authorities);
            authentication.setDetails(username);
            
            return authentication;
        } catch (Exception e) {
            return null;
        }
    }
}