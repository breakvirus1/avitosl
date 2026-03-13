package com.example.avito.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/actuator/health/**", 
                    "/actuator/info").permitAll()
                
                // Эндпоинты для просмотра объявлений и категорий (только чтение)
                .requestMatchers(
                    "/api/posts/**",
                    "/api/categories/**",
                    "/api/comments/post/**",
                    "/api/photos/post/**"
                ).permitAll()
                
                // Эндпоинты для создания/редактирования требуют аутентификации
                .requestMatchers(
                    "/api/posts",
                    "/api/posts/*",
                    "/api/comments",
                    "/api/comments/*",
                    "/api/photos",
                    "/api/photos/*"
                ).authenticated()
                
                .requestMatchers("/api/auth/**").permitAll()
                
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            java.util.Set<org.springframework.security.core.GrantedAuthority> authorities = new java.util.HashSet<>();
            
            java.util.Map<String, Object> claims = jwt.getClaims();
            
            // Логируем все claims для отладки
            org.slf4j.LoggerFactory.getLogger(WebSecurityConfig.class)
                .debug("JWT claims: {}", claims);
            
            // realm_access.roles (реальные роли без префикса клиента)
            if (claims.containsKey("realm_access")) {
                java.util.Map<String, Object> realmAccess = (java.util.Map<String, Object>) claims.get("realm_access");
                if (realmAccess.containsKey("roles")) {
                    java.util.List<String> roles = (java.util.List<String>) realmAccess.get("roles");
                    org.slf4j.LoggerFactory.getLogger(WebSecurityConfig.class)
                        .debug("Found realm roles: {}", roles);
                    for (String role : roles) {
                        authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                    }
                }
            }
            
            // resource_access.*.roles - извлекаем роли из всех клиентов, убирая префикс клиента
            if (claims.containsKey("resource_access")) {
                java.util.Map<String, Object> resourceAccess = (java.util.Map<String, Object>) claims.get("resource_access");
                for (java.util.Map.Entry<String, Object> entry : resourceAccess.entrySet()) {
                    String clientName = entry.getKey();
                    java.util.Map<String, Object> clientData = (java.util.Map<String, Object>) entry.getValue();
                    if (clientData.containsKey("roles")) {
                        java.util.List<String> roles = (java.util.List<String>) clientData.get("roles");
                        org.slf4j.LoggerFactory.getLogger(WebSecurityConfig.class)
                            .debug("Found client '{}' roles: {}", clientName, roles);
                        for (String role : roles) {
                            // Убираем префикс клиента, если роль начинается с него
                            String cleanRole = role;
                            if (role.toLowerCase().startsWith(clientName.toLowerCase())) {
                                cleanRole = role.substring(clientName.length());
                            }
                            // Добавляем префикс ROLE_ для Spring Security
                            authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + cleanRole.toUpperCase()));
                        }
                    }
                }
            }
            
            // groups
            if (claims.containsKey("groups")) {
                java.util.List<String> groups = (java.util.List<String>) claims.get("groups");
                org.slf4j.LoggerFactory.getLogger(WebSecurityConfig.class)
                    .debug("Found groups: {}", groups);
                for (String group : groups) {
                    authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + group.toUpperCase()));
                }
            }
            
            org.slf4j.LoggerFactory.getLogger(WebSecurityConfig.class)
                .debug("Extracted authorities: {}", authorities);
            
            return authorities;
        });
        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://127.0.0.1:5173"
        ));
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin"
        ));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}