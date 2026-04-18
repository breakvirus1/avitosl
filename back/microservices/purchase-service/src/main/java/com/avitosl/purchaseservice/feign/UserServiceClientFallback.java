package com.avitosl.purchaseservice.feign;

import org.springframework.stereotype.Component;
import com.avitosl.purchaseservice.response.UserResponse;

@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public UserResponse getUserById(Long id) {
        throw new RuntimeException("User service is unavailable. Cannot fetch user with id: " + id);
    }

    @Override
    public UserResponse getUserByKeycloakId(String keycloakId) {
        throw new RuntimeException("User service is unavailable. Cannot fetch user with keycloakId: " + keycloakId);
    }

    @Override
    public UserResponse addFundsToWallet(Long id, Double amount) {
        throw new RuntimeException("User service is unavailable. Cannot add funds to wallet for user id: " + id);
    }

    @Override
    public UserResponse subtractFromWallet(Long id, Double amount) {
        throw new RuntimeException("User service is unavailable. Cannot subtract funds from wallet for user id: " + id);
    }
}