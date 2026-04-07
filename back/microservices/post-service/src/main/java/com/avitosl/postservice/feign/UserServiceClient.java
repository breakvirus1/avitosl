package com.avitosl.postservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.avitosl.postservice.response.UserResponse;

@FeignClient(name = "user-service", path = "/api/users",
             configuration = FeignConfig.class,
             fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @GetMapping("/{id}")
    UserResponse getUserById(@PathVariable Long id);

    @GetMapping("/username/{username}")
    UserResponse getUserByUsername(@PathVariable String username);

    @GetMapping
    java.util.List<UserResponse> getAllUsers();

    @GetMapping("/search")
    java.util.List<UserResponse> searchUsers(@RequestParam String query);
}