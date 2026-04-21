package com.avitosl.userservice.security;

import com.avitosl.userservice.service.AuthService;
import com.avitosl.userservice.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private FilterChain filterChain;

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        Mockito.reset(authService, userService, filterChain);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void doFilterInternal_ShouldSkipAuth_ForAuthEndpoints() throws ServletException, IOException {
        // Given
        request.setServletPath("/api/auth/login");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(authService, userService);
    }

    @Test
    void doFilterInternal_ShouldSetAuthentication_WhenValidJwtToken() throws ServletException, IOException {
        // Given
        request.setServletPath("/api/users/1");
        request.addHeader("Authorization", "Bearer valid-jwt-token");
        when(authService.validateToken("valid-jwt-token")).thenReturn(true);
        when(authService.getUsernameFromToken("valid-jwt-token")).thenReturn("testuser");
        when(authService.getUserIdFromToken("valid-jwt-token")).thenReturn(1L);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(authService).validateToken("valid-jwt-token");
        verify(authService).getUsernameFromToken("valid-jwt-token");
        verify(authService).getUserIdFromToken("valid-jwt-token");
        verify(userService, never()).getUserByKeycloakId(any());
        verify(filterChain).doFilter(request, response);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("testuser");
        assertThat(request.getAttribute("userId")).isEqualTo(1L);
    }

    @Test
    void doFilterInternal_ShouldSetAuthentication_WhenValidKeycloakToken() throws ServletException, IOException {
        // Given
        request.setServletPath("/api/users/1");
        String token = createMockKeycloakToken("kc-123-id", "testuser");
        request.addHeader("Authorization", "Bearer " + token);
        when(authService.validateToken(token)).thenReturn(false);
        com.avitosl.userservice.entity.User user = mock(com.avitosl.userservice.entity.User.class);
        when(user.getId()).thenReturn(2L);
        when(user.getUsername()).thenReturn("testuser");
        when(userService.getUserByKeycloakId("kc-123-id")).thenReturn(user);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(authService).validateToken(token);
        verify(userService).getUserByKeycloakId("kc-123-id");
        verify(filterChain).doFilter(request, response);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("testuser");
        assertThat(request.getAttribute("userId")).isEqualTo(2L);
    }

    @Test
    void doFilterInternal_ShouldSkipAuth_WhenNoAuthHeader() throws ServletException, IOException {
        // Given
        request.setServletPath("/api/users/1");
        // No Authorization header

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(authService, never()).validateToken(any());
        verify(userService, never()).getUserByKeycloakId(any());
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_ShouldContinueChain_WhenKeycloakTokenInvalid() throws ServletException, IOException {
        // Given
        request.setServletPath("/api/users/1");
        request.addHeader("Authorization", "Bearer bad-token");
        when(authService.validateToken("bad-token")).thenReturn(false);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(authService).validateToken("bad-token");
        verify(userService, never()).getUserByKeycloakId(any());
        verify(filterChain).doFilter(request, response);
    }

    private String createMockKeycloakToken(String sub, String username) {
        String payload = String.format("{\"sub\":\"%s\",\"username\":\"%s\"}", sub, username);
        String base64Payload = Base64.getEncoder().encodeToString(payload.getBytes());
        return "header." + base64Payload + ".signature";
    }
}
