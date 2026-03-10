package com.example.avito.controller;

import com.example.avito.entity.User;
import com.example.avito.mapper.UserMapper;
import com.example.avito.request.LoginRequest;
import com.example.avito.request.RegisterRequest;
import com.example.avito.response.UserResponse;
import com.example.avito.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация и авторизация", description = "API для управления аутентификацией и авторизацией пользователей")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

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
            )
        }
    )
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody
        @Parameter(description = "Данные для регистрации", required = true)
        RegisterRequest request) {

        if (userService.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Пользователь с таким email уже существует");
        }

        UserResponse response = userService.createUser(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Вход в систему",
        description = "Аутентифицирует пользователя по email и паролю",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Успешная аутентификация",
                content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Неверные учетные данные"
            )
        }
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody
        @Parameter(description = "Логин и пароль пользователя", required = true)
        LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userService.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        UserResponse response = userMapper.toResponse(user);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Получение данных текущего пользователя",
        description = "Возвращает информацию о текущем авторизованном пользователе",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Данные пользователя успешно получены",
                content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Пользователь не авторизован"
            )
        }
    )
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Не авторизован");
        }

        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        UserResponse userResponse = userMapper.toResponse(user);
        return ResponseEntity.ok(userResponse);
    }
}
