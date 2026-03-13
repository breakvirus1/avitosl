package com.example.avito.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@Tag(name = "Тестовые эндпоинты", description = "Эндпоинты для тестирования безопасности")
public class TestController {
    
    @Operation(
        summary = "Публичный эндпоинт",
        description = "Доступен без аутентификации",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Успешный ответ",
                content = @Content(schema = @Schema(type = "string"))
            )
        }
    )
    @GetMapping("/api/public")
    public String publicEndpoint() {
        return "Это публичный эндпоинт";
    }

    @Operation(
        summary = "Эндпоинт для пользователей",
        description = "Доступен только пользователям с ролью 'user'",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Успешный ответ",
                content = @Content(schema = @Schema(type = "string"))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Доступ запрещен"
            )
        }
    )
    @GetMapping("/api/user")
    @PreAuthorize("hasRole('ROLE_USER')")
    public String userEndpoint() {
        return "Привет, обычный пользователь!";
    }

    @Operation(
        summary = "Эндпоинт для администраторов",
        description = "Доступен только администраторам с ролью 'avitoadmin'",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Успешный ответ",
                content = @Content(schema = @Schema(type = "string"))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Доступ запрещен"
            )
        }
    )
    @GetMapping("/api/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminEndpoint() {
        return "Добро пожаловать, администратор!";
    }
}
