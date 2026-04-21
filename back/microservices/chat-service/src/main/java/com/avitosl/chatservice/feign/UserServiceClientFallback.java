package com.avitosl.chatservice.feign;

import org.springframework.stereotype.Component;
import com.avitosl.chatservice.response.UserResponse;

@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public UserResponse getUserById(Long id) {
        throw new RuntimeException("User service is unavailable. Cannot fetch user with id: " + id);
    }
}