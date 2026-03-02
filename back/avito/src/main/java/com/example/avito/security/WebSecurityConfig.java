package com.example.avito.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final JwtAuthenticationTokenConverter jwtAuthenticationTokenConverter;

    public WebSecurityConfig(JwtAuthenticationTokenConverter jwtAuthenticationTokenConverter) {
        this.jwtAuthenticationTokenConverter = jwtAuthenticationTokenConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        // .requestMatchers("/api/**").hasRole("USER")
                        // .requestMatchers(null)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-ui/index.html",
                                "/swagger-resources/**", 
                                "/configuration/ui", 
                                "/configuration/security" 
                        ).permitAll()

                        .anyRequest().authenticated())//denyAll())
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer.jwt(
                        jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationTokenConverter)))
                .sessionManagement(
                        sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())
                .build();
    }
}