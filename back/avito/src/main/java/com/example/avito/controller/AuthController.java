package com.example.avito.controller;

import com.example.avito.entity.User;
import com.example.avito.mapper.UserMapper;
import com.example.avito.request.RegisterRequest;
import com.example.avito.response.UserResponse;
import com.example.avito.service.*;
import com.example.avito.security.UserSecurityService;
import com.example.avito.exception.ConflictException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Аутентификация и авторизация", description = "API для управления аутентификацией и авторизацией пользователей")
public class AuthController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final UserSecurityService userSecurityService;
    private final KeycloakService keycloakService;

    @Operation(
        summary = "Регистрация нового пользователя",
        description = "Создает новый аккаунт пользователя в системе",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Пользователь успешно зарегистрирован",
                content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Ошибка валидации или пользователь с таким email уже существует"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody
        @Parameter(description = "Данные для регистрации", required = true)
        @Valid RegisterRequest request) {

        if (userService.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }

        String keycloakUserId = keycloakService.createUser(
            request.getEmail(),
            request.getFirstName(),
            request.getPassword()
        );
        
        keycloakService.assignRoleToUser(keycloakUserId, "USER");

        UserResponse response = userService.createUser(request);
        
        userService.updateKeycloakId(response.getId(), keycloakUserId);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Получение данных текущего пользователя",
        description = "Возвращает информацию о текущем авторизованном пользователе на основе JWT токена",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Данные пользователя успешно получены",
                content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Пользователь не аутентифицирован"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        User user = userSecurityService.getCurrentUserByEmail(email);
        UserResponse response = userMapper.toResponse(user);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Получение ролей текущего пользователя",
        description = "Возвращает список ролей из JWT токена текущего авторизованного пользователя",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Роли успешно получены",
                content = @Content(schema = @Schema(implementation = java.util.List.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Пользователь не аутентифицирован"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @GetMapping("/roles")
    public ResponseEntity<java.util.List<String>> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        java.util.List<String> roles = new java.util.ArrayList<>();
        
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if (role.startsWith("ROLE_")) {
                role = role.substring(5);
            }
            roles.add(role);
        }
        
        return ResponseEntity.ok(roles);
    }
}
