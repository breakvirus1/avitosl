package com.avitosl.postservice.feign;

import org.springframework.stereotype.Component;
import com.avitosl.postservice.response.UserResponse;

import java.util.List;

@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public UserResponse getUserById(Long id) {
        // Возвращаем заглушку или выбрасываем исключение
        throw new RuntimeException("User service is unavailable. Cannot fetch user with id: " + id);
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        throw new RuntimeException("User service is unavailable. Cannot fetch user with username: " + username);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        // Return a default user when service is unavailable
        UserResponse defaultUser = new UserResponse();
        defaultUser.setId(1L);
        defaultUser.setUsername("default");
        defaultUser.setEmail("default@example.com");
        defaultUser.setKeycloakId("default-keycloak-id");
        return List.of(defaultUser);
    }

    @Override
    public List<UserResponse> searchUsers(String query) {
        throw new RuntimeException("User service is unavailable. Cannot search users with query: " + query);
    }

    @Override
    public UserResponse getUserByKeycloakId(String keycloakId) {
        throw new RuntimeException("User service is unavailable. Cannot fetch user with keycloakId: " + keycloakId);
    }
}