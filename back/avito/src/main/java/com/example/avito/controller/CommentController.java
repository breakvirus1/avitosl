package com.example.avito.controller;

import com.example.avito.entity.User;
import com.example.avito.request.CommentRequest;
import com.example.avito.response.CommentResponse;
import com.example.avito.security.UserSecurityService;
import com.example.avito.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Validated
@Tag(name = "Комментарии", description = "API для управления комментариями к объявлениям")
public class CommentController {

    private final CommentService commentService;
    private final UserSecurityService userSecurityService;

    private User getCurrentUser() {
        return userSecurityService.getCurrentUser();
    }

    @Operation(
        summary = "Получение комментариев к объявлению",
        description = "Возвращает список всех комментариев для указанного объявления",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Комментарии успешно получены",
                content = @Content(schema = @Schema(implementation = CommentResponse.class, type = "array"))
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(
            @Parameter(description = "ID объявления", in = ParameterIn.PATH, required = true, schema = @Schema(type = "integer"))
            @PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }

    @Operation(
        summary = "Получение комментариев пользователя",
        description = "Возвращает список всех комментариев, оставленных указанным пользователем. Доступно только администраторам",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Комментарии пользователя успешно получены",
                content = @Content(schema = @Schema(implementation = CommentResponse.class, type = "array"))
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
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CommentResponse>> getCommentsByUser(
            @Parameter(description = "ID пользователя", in = ParameterIn.PATH, required = true, schema = @Schema(type = "integer"))
            @PathVariable Long userId) {
        return ResponseEntity.ok(commentService.getCommentsByUser(userId));
    }

    @Operation(
        summary = "Получение комментария по ID",
        description = "Возвращает информацию о комментарии по его идентификатору",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Комментарий успешно найден",
                content = @Content(schema = @Schema(implementation = CommentResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Комментарий не найден"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<CommentResponse> getCommentById(
            @Parameter(description = "ID комментария", in = ParameterIn.PATH, required = true, schema = @Schema(type = "integer"))
            @PathVariable Long id) {
        return ResponseEntity.ok(commentService.getCommentById(id));
    }

    @Operation(
        summary = "Создание комментария",
        description = "Создает новый комментарий к объявлению. Требует аутентификации. Автором комментария является текущий пользователь",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Комментарий успешно создан",
                content = @Content(schema = @Schema(implementation = CommentResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Ошибка валидации данных"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> createComment(@RequestBody
        @Parameter(description = "Данные комментария", required = true)
        @Valid CommentRequest request) {
        User author = getCurrentUser();
        return ResponseEntity.ok(commentService.createComment(request, author));
    }

    @Operation(
        summary = "Обновление комментария",
        description = "Обновляет существующий комментарий. Требует аутентификации. Пользователь может редактировать только свои комментарии",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Комментарий успешно обновлен",
                content = @Content(schema = @Schema(implementation = CommentResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Нет прав на редактирование этого комментария"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Комментарий не найден"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> updateComment(
            @Parameter(description = "ID комментария", in = ParameterIn.PATH, required = true, schema = @Schema(type = "integer"))
            @PathVariable Long id,
            @RequestBody
            @Parameter(description = "Обновленные данные комментария", required = true)
            @Valid CommentRequest request) {
        User author = getCurrentUser();
        return ResponseEntity.ok(commentService.updateComment(id, request, author));
    }

    @Operation(
        summary = "Удаление комментария",
        description = "Удаляет комментарий. Требует аутентификации. Пользователь может удалять только свои комментарии",
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "Комментарий успешно удален"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Нет прав на удаление этого комментария"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Комментарий не найден"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "ID комментария", in = ParameterIn.PATH, required = true, schema = @Schema(type = "integer"))
            @PathVariable Long id) {
        User currentUser = getCurrentUser();
        commentService.deleteComment(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}