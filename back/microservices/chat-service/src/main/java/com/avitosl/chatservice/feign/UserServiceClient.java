package com.avitosl.chatservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.avitosl.chatservice.response.UserResponse;

@FeignClient(name = "user-service", path = "/api/users",
             configuration = FeignConfig.class,
             fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @GetMapping("/{id}")
    UserResponse getUserById(@PathVariable Long id);
}