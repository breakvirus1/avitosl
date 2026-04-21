package com.avitosl.userservice.service;

import com.avitosl.userservice.entity.User;
import com.avitosl.userservice.exception.NotFoundException;
import com.avitosl.userservice.request.LoginRequest;
import com.avitosl.userservice.request.RegisterRequest;
import com.avitosl.userservice.response.AuthResponse;
import com.avitosl.userservice.response.UserResponse;
import com.avitosl.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.JwtException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<String> usernameCaptor;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setUsername("testuser");
        validRegisterRequest.setEmail("test@example.com");
        validRegisterRequest.setPassword("password123");
        validRegisterRequest.setFirstName("Test");
        validRegisterRequest.setLastName("User");
        validRegisterRequest.setPhoneNumber("+1234567890");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setUsername("testuser");
        validLoginRequest.setPassword("password123");

        savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        savedUser.setEmail("test@example.com");
        savedUser.setPasswordHash("encodedPassword");
        savedUser.setFirstName("Test");
        savedUser.setLastName("User");
        savedUser.setPhoneNumber("+1234567890");
        savedUser.setEnabled(true);
        savedUser.setWalletBalance(0.0);
        savedUser.setCreatedAt(LocalDateTime.now());
        savedUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void register_ShouldCreateUserAndReturnAuthResponse_WhenUsernameAndEmailAreUnique() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            return user;
        });

        // When
        AuthResponse response = authService.register(validRegisterRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(86400000L, response.getExpiresIn());
        assertNotNull(response.getUser());

        assertEquals("testuser", response.getUser().getUsername());
        assertEquals("test@example.com", response.getUser().getEmail());
        assertEquals("Test", response.getUser().getFirstName());
        assertEquals("User", response.getUser().getLastName());
        assertTrue(response.getUser().getEnabled());

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenUsernameAlreadyExists() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(validRegisterRequest);
        });

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(validRegisterRequest);
        });

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        // When
        AuthResponse response = authService.login(validLoginRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(86400000L, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals("testuser", response.getUser().getUsername());
        assertEquals("test@example.com", response.getUser().getEmail());

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
    }

    @Test
    void login_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            LoginRequest req = new LoginRequest();
            req.setUsername("nonexistent");
            req.setPassword("password");
            authService.login(req);
        });

        assertEquals("Invalid username or password", exception.getMessage());
        verify(userRepository).findByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsInvalid() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            LoginRequest req = new LoginRequest();
            req.setUsername("testuser");
            req.setPassword("wrongpassword");
            authService.login(req);
        });

        assertEquals("Invalid username or password", exception.getMessage());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("wrongpassword", "encodedPassword");
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenTokenIsValid() {
        // Given - generate a valid token
        String token = Jwts.builder()
                .setSubject("testuser")
                .claim("userId", 1L)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, "avito-secret-key-for-jwt-tokens-must-be-at-least-256-bits-long".getBytes(StandardCharsets.UTF_8))
                .compact();

        // When
        boolean isValid = authService.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsExpired() {
        // Given - generate an expired token
        String token = Jwts.builder()
                .setSubject("testuser")
                .claim("userId", 1L)
                .setIssuedAt(new Date(System.currentTimeMillis() - 86400000))
                .setExpiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(SignatureAlgorithm.HS256, "avito-secret-key-for-jwt-tokens-must-be-at-least-256-bits-long".getBytes(StandardCharsets.UTF_8))
                .compact();

        // When
        boolean isValid = authService.validateToken(token);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsMalformed() {
        // Given
        String malformedToken = "invalid.token.here";

        // When
        boolean isValid = authService.validateToken(malformedToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void getUsernameFromToken_ShouldReturnUsername_WhenTokenIsValid() {
        // Given - generate valid token
        String token = Jwts.builder()
                .setSubject("testuser")
                .claim("userId", 1L)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, "avito-secret-key-for-jwt-tokens-must-be-at-least-256-bits-long".getBytes(StandardCharsets.UTF_8))
                .compact();

        // When
        String username = authService.getUsernameFromToken(token);

        // Then
        assertEquals("testuser", username);
    }

    @Test
    void getUserIdFromToken_ShouldReturnUserId_WhenTokenIsValid() {
        // Given
        String token = Jwts.builder()
                .setSubject("testuser")
                .claim("userId", 42L)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, "avito-secret-key-for-jwt-tokens-must-be-at-least-256-bits-long".getBytes(StandardCharsets.UTF_8))
                .compact();

        // When
        Long userId = authService.getUserIdFromToken(token);

        // Then
        assertEquals(42L, userId);
    }

    @Test
    void getUsernameFromToken_ShouldThrowException_WhenTokenIsInvalid() {
        // Given
        String invalidToken = "invalid.token";

        // When/Then
        assertThrows(JwtException.class, () -> {
            authService.getUsernameFromToken(invalidToken);
        });
    }

    @Test
    void getUserIdFromToken_ShouldThrowException_WhenTokenIsInvalid() {
        // Given
        String invalidToken = "invalid.token";

        // When/Then
        assertThrows(JwtException.class, () -> {
            authService.getUserIdFromToken(invalidToken);
        });
    }
}
