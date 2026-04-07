package com.avitosl.userservice.service;

import com.avitosl.userservice.entity.User;
import com.avitosl.userservice.exception.NotFoundException;
import com.avitosl.userservice.repository.UserRepository;
import com.avitosl.userservice.request.LoginRequest;
import com.avitosl.userservice.request.RegisterRequest;
import com.avitosl.userservice.response.AuthResponse;
import com.avitosl.userservice.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import io.jsonwebtoken.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String jwtSecret = "avito-secret-key-for-jwt-tokens-must-be-at-least-256-bits-long";
    private final long jwtExpirationInMs = 86400000;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEnabled(true);
        user.setWalletBalance(0.0);

        // Set default role (USER)
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        user.setRoles(new HashSet<>()); // Will be populated by RoleService

        User savedUser = userRepository.save(user);
        UserResponse userResponse = new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getPhoneNumber(),
                savedUser.getWalletBalance(),
                savedUser.getEnabled(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt()
        );

        String token = generateJwtToken(savedUser.getUsername(), savedUser.getId());

        return new AuthResponse(token, "Bearer", jwtExpirationInMs, userResponse);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getWalletBalance(),
                user.getEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );

        String token = generateJwtToken(user.getUsername(), user.getId());

        return new AuthResponse(token, "Bearer", jwtExpirationInMs, userResponse);
    }

    private String generateJwtToken(String username, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.get("userId", Long.class);
    }
}
