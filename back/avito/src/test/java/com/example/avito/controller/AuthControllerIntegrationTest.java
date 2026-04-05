package com.example.avito.controller;

import com.example.avito.entity.User;
import com.example.avito.request.RegisterRequest;
import com.example.avito.response.UserResponse;
import com.example.avito.service.KeycloakService;
import com.example.avito.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private KeycloakService keycloakService;

    @InjectMocks
    private AuthController authController;

    @Test
    void register_shouldCreateUserSuccessfully() {
        // Given
        RegisterRequest request = new RegisterRequest("John", "john@example.com", "password123", "password123", "1234567890");
        UserResponse expectedResponse = UserResponse.builder()
                .email("john@example.com")
                .firstName("John")
                .build();

        when(keycloakService.createUser(anyString(), anyString(), anyString())).thenReturn("keycloak-id");
        when(userService.createUser(request)).thenReturn(expectedResponse);
        doNothing().when(userService).updateKeycloakId(any(), anyString());

        // When
        var response = authController.register(request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getEmail()).isEqualTo("john@example.com");
    }
}