package com.example.avito.controller;

import com.example.avito.entity.Role;
import com.example.avito.entity.User;
import com.example.avito.response.UserResponse;
import com.example.avito.service.KeycloakService;
import com.example.avito.service.RoleService;
import com.example.avito.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "Пользователи", description = "API для управления пользователями")
public class UserController {

    private final UserService userService;
    private final KeycloakService keycloakService;
    private final RoleService roleService;

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
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
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
        summary = "Получение текущего пользователя",
        description = "Возвращает информацию о текущем аутентифицированном пользователе",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Пользователь успешно найден",
                content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Пользователь не аутентифицирован"
            )
        }
    )
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
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
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
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
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
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
            @Valid com.example.avito.request.RegisterRequest request) {
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
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
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

    @Operation(
        summary = "Синхронизация пользователей из Keycloak",
        description = "Загружает всех пользователей из Keycloak и сохраняет их в локальной базе данных. Требует роль ADMIN.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Синхронизация завершена",
                content = @Content(schema = @Schema(type = "string"))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Доступ запрещен"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @PostMapping("/sync-from-keycloak")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> syncUsersFromKeycloak() {
        List<UserRepresentation> keycloakUsers = keycloakService.getAllUsers();
        int syncedCount = 0;
        int skippedCount = 0;
        
        Role userRole = roleService.getRoleByName("USER");
        
        for (UserRepresentation kcUser : keycloakUsers) {
            try {
                if (userService.findByEmail(kcUser.getEmail()).isPresent()) {
                    skippedCount++;
                    continue;
                }

                User user = User.builder()
                        .email(kcUser.getEmail())
                        .firstName(kcUser.getFirstName())
                        .phoneNumber(kcUser.getAttributes() != null && kcUser.getAttributes().containsKey("phone")
                            ? kcUser.getAttributes().get("phone").get(0)
                            : null)
                        .keycloakId(kcUser.getId())
                        .enabled(kcUser.isEnabled())
                        .password("KEYCLOAK_AUTH") // Dummy password for Keycloak-synced users
                        .build();
                
                user.getRoles().add(userRole);
                userService.saveUser(user);
                syncedCount++;
            } catch (Exception e) {
                // Пропускаем пользователя при ошибке
            }
        }
        
        return ResponseEntity.ok("Синхронизировано: " + syncedCount + ", пропущено: " + skippedCount);
    }
}