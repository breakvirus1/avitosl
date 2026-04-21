package com.avitosl.userservice.controller;

import com.avitosl.userservice.security.SecurityConfig;
import com.avitosl.userservice.entity.User;
import com.avitosl.userservice.exception.NotFoundException;
import com.avitosl.userservice.response.UserResponse;
import com.avitosl.userservice.security.JwtAuthenticationFilter;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getUserById_ShouldReturnUserResponse_WhenExists() throws Exception {
        // Given
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setWalletBalance(100.0);
        user.setEnabled(true);

        UserResponse userResponse = new UserResponse(
                1L, "testuser", "test@example.com", "Test", "User",
                null, null, 100.0, true, null, null
        );
        when(userService.getUserById(1L)).thenReturn(user);

        // When/Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        // Given
        when(userService.getUserById(999L)).thenThrow(new NotFoundException("User not found"));

        // When/Then
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsers_ShouldReturnList() throws Exception {
        // Given
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setWalletBalance(0.0);
        user1.setEnabled(true);

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setWalletBalance(0.0);
        user2.setEnabled(true);

        when(userService.getAllUsers()).thenReturn(Arrays.asList(user1, user2));

        // When/Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("user2"));
    }

    @Test
    @WithMockUser
    void createUser_ShouldReturnUserResponse_WhenValid() throws Exception {
        // Given
        User user = new User();
        user.setUsername("newuser");
        user.setEmail("new@example.com");
        user.setFirstName("New");
        user.setLastName("User");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("newuser");
        savedUser.setEmail("new@example.com");
        savedUser.setFirstName("New");
        savedUser.setLastName("User");
        savedUser.setWalletBalance(0.0);
        savedUser.setEnabled(true);

        when(userService.createUser(any(User.class))).thenReturn(savedUser);

        // When/Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @WithMockUser
    void updateUser_ShouldUpdateAndReturn_WhenExists() throws Exception {
        // Given
        User updateDetails = new User();
        updateDetails.setUsername("updated");
        updateDetails.setEmail("updated@example.com");

        User existing = new User();
        existing.setId(1L);
        existing.setUsername("old");
        existing.setEmail("old@example.com");

        User updated = new User();
        updated.setId(1L);
        updated.setUsername("updated");
        updated.setEmail("updated@example.com");

        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(updated);

        // When/Then
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updated"));
    }

    @Test
    @WithMockUser
    void deleteUser_ShouldReturnNoContent_WhenExists() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(1L);

        // When/Then
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getUserByUsername_ShouldReturnUser_WhenExists() throws Exception {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        when(userService.getUserByUsername("testuser")).thenReturn(user);

        // When/Then
        mockMvc.perform(get("/api/users/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void getUserByKeycloakId_ShouldReturnUser_WhenExists() throws Exception {
        // Given
        User user = new User();
        user.setKeycloakId("kc123");
        user.setUsername("testuser");
        when(userService.getUserByKeycloakId("kc123")).thenReturn(user);

        // When/Then
        mockMvc.perform(get("/api/users/keycloak/kc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }
}
