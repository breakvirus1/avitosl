package com.example.avito.controller;

import com.example.avito.dto.response.UserResponse;
import com.example.avito.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Controller", description = "Operations pertaining to users in Avito application")
public class UserController {
    @Autowired
    private UserService userService;

    @Operation(summary = "Get current user")
    @GetMapping("/me")
    public UserResponse getCurrentUser() {
        return userService.getCurrentUser();
    }

    @Operation(summary = "Find user by email")
    @GetMapping("/{email}")
    public UserResponse findByEmail(@PathVariable String email) {
        return userService.findByEmail(email);
    }
}


