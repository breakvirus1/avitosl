package com.avitosl.purchaseservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.avitosl.purchaseservice.response.UserResponse;

@FeignClient(name = "user-service", path = "/api/users",
             configuration = FeignConfig.class,
             fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @GetMapping("/{id}")
    UserResponse getUserById(@PathVariable Long id);

    @GetMapping("/keycloak/{keycloakId}")
    UserResponse getUserByKeycloakId(@PathVariable String keycloakId);

    @PostMapping("/{id}/wallet/add")
    UserResponse addFundsToWallet(@PathVariable Long id, @RequestParam Double amount);

    @PostMapping("/{id}/wallet/subtract")
    UserResponse subtractFromWallet(@PathVariable Long id, @RequestParam Double amount);
}