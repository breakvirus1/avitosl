package com.avitosl.userservice.controller;

import com.avitosl.userservice.security.JwtAuthenticationFilter;
import com.avitosl.userservice.security.SecurityConfig;
import com.avitosl.userservice.request.LoginRequest;
import com.avitosl.userservice.request.RegisterRequest;
import com.avitosl.userservice.response.AuthResponse;
import com.avitosl.userservice.service.AuthService;
import com.avitosl.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void register_ShouldReturnAuthResponse_WhenRequestIsValid() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setPhoneNumber("+1234567890");

        AuthResponse mockResponse = new AuthResponse(
                "jwt-token",
                "Bearer",
                86400000L,
                new com.avitosl.userservice.response.UserResponse(
                        1L,
                        "testuser",
                        "test@example.com",
                        "Test",
                        "User",
                        "+1234567890",
                        null,
                        0.0,
                        true,
                        null,
                        null
                )
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        // When/Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$.token").value("jwt-token"))
                 .andExpect(jsonPath("$.type").value("Bearer"))
                 .andExpect(jsonPath("$.expiresIn").value(86400000))
                 .andExpect(jsonPath("$.user.username").value("testuser"))
                 .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        AuthResponse mockResponse = new AuthResponse(
                "jwt-token",
                "Bearer",
                86400000L,
                new com.avitosl.userservice.response.UserResponse(
                        1L,
                        "testuser",
                        "test@example.com",
                        "Test",
                        "User",
                        "+1234567890",
                        null,
                        0.0,
                        true,
                        null,
                        null
                )
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.username").value("testuser"));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenRequestIsInvalid() throws Exception {
        // Given - empty request (violates @NotBlank constraints)
        RegisterRequest request = new RegisterRequest();

        // When/Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ShouldReturnBadRequest_WhenRequestIsInvalid() throws Exception {
        // Given - empty request
        LoginRequest request = new LoginRequest();

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
