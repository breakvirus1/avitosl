package com.example.avito.controller;

import com.example.avito.response.UserResponse;
import com.example.avito.security.UserSecurityService;
import com.example.avito.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "API для управления пользователями")
public class UserController {

    private final UserService userService;
    private final UserSecurityService userSecurityService;

    @Operation(
        summary = "Получение пользователя по ID",
        description = "Возвращает информацию о пользователе по его идентификатору",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Пользователь успешно найден",
                content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Пользователь не найден"
            )
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "ID пользователя", in = ParameterIn.PATH, required = true, schema = @Schema(type = "integer"))
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(
        summary = "Получение списка всех пользователей",
        description = "Возвращает список всех пользователей системы. Доступно только администраторам",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Список пользователей успешно получен",
                content = @Content(schema = @Schema(implementation = UserResponse.class, type = "array"))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Доступ запрещен. Требуется роль администратора"
            )
        }
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(
        summary = "Обновление данных пользователя",
        description = "Обновляет информацию о пользователе. Пользователь может редактировать только свои данные, администратор - любые",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Пользователь успешно обновлен",
                content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Ошибка валидации данных"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Нет прав на редактирование этого пользователя"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Пользователь не найден"
            )
        }
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurityService.isCurrentUser(#id)")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "ID пользователя", in = ParameterIn.PATH, required = true, schema = @Schema(type = "integer"))
            @PathVariable Long id,
            @RequestBody
            @Parameter(description = "Обновленные данные пользователя", required = true)
            com.example.avito.request.RegisterRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @Operation(
        summary = "Удаление пользователя",
        description = "Удаляет пользователя из системы. Пользователь может удалять только свой аккаунт, администратор - любой",
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "Пользователь успешно удален"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Нет прав на удаление этого пользователя"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Пользователь не найден"
            )
        }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurityService.isCurrentUser(#id)")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя", in = ParameterIn.PATH, required = true, schema = @Schema(type = "integer"))
            @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}