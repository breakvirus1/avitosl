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
        throw new RuntimeException("User service is unavailable. Cannot fetch all users");
    }

    @Override
    public List<UserResponse> searchUsers(String query) {
        throw new RuntimeException("User service is unavailable. Cannot search users with query: " + query);
    }
}