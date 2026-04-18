package com.avitosl.userservice.controller;

import com.avitosl.userservice.entity.User;
import com.avitosl.userservice.response.UserResponse;
import com.avitosl.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(mapToResponse(createdUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(mapToResponse(user));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(mapToResponse(user));
    }

    @GetMapping("/keycloak/{keycloakId}")
    public ResponseEntity<UserResponse> getUserByKeycloakId(@PathVariable String keycloakId) {
        User user = userService.getUserByKeycloakId(keycloakId);
        return ResponseEntity.ok(mapToResponse(user));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponse> responses = users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(mapToResponse(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/wallet/add")
    public ResponseEntity<UserResponse> addFundsToWallet(@PathVariable Long id, @RequestParam Double amount,
                                                          HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        boolean internalService = "purchase-service".equals(request.getHeader("X-Internal-Service"));
        if (currentUserId != null && !id.equals(currentUserId) && !internalService) {
            throw new AuthorizationServiceException("Cannot modify another user's wallet");
        }
        User user = userService.addFundsToWallet(id, amount);
        return ResponseEntity.ok(mapToResponse(user));
    }

    @PostMapping("/{id}/wallet/subtract")
    public ResponseEntity<UserResponse> subtractFromWallet(@PathVariable Long id, @RequestParam Double amount,
                                                            HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        boolean internalService = "purchase-service".equals(request.getHeader("X-Internal-Service"));
        if (currentUserId != null && !id.equals(currentUserId) && !internalService) {
            throw new AuthorizationServiceException("Cannot modify another user's wallet");
        }
        User user = userService.subtractFromWallet(id, amount);
        return ResponseEntity.ok(mapToResponse(user));
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getKeycloakId(),
                user.getWalletBalance(),
                user.getEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
