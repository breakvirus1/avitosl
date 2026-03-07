package com.example.avito.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class TestController {
    @GetMapping("/api/public")
    public String publicEndpoint() {
        return "Это публичный эндпоинт";
    }

    @GetMapping("/api/user")
    @PreAuthorize("hasRole('user')")
    public String userEndpoint() {
        return "Привет, обычный пользователь!";
    }

    @GetMapping("/api/admin")
    @PreAuthorize("hasRole('avitoadmin')")
    public String adminEndpoint() {
        return "Добро пожаловать, администратор!";
    }

}
