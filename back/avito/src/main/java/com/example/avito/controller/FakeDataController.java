package com.example.avito.controller;

import com.example.avito.service.FakeDataGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fake-data")
@RequiredArgsConstructor
@Tag(name = "Фейковые данные", description = "API для генерации тестовых данных (только для разработки)")
public class FakeDataController {

    private final FakeDataGeneratorService fakeDataGeneratorService;

    @Operation(
        summary = "Генерация фейковых объявлений",
        description = "Создает указанное количество фейковых объявлений с комментариями. Требует роль ADMIN. Данные генерируются на основе существующих пользователей, категорий и подкатегорий",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Фейковые данные успешно созданы",
                content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Ошибка валидации параметров"
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
    @PostMapping("/posts/{count}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> generateFakePosts(
            @Parameter(description = "Количество объявлений для создания", required = true, schema = @Schema(type = "integer", minimum = "1", maximum = "1000"))
            @PathVariable int count) {
        
        if (count <= 0 || count > 1000) {
            return ResponseEntity.badRequest().body("Количество должно быть от 1 до 1000");
        }
        
        fakeDataGeneratorService.generateFakePosts(count);
        return ResponseEntity.ok("Создано " + count + " фейковых объявлений");
    }

    @Operation(
        summary = "Очистка всех объявлений",
        description = "Удаляет все объявления и связанные с ними комментарии. Требует роль ADMIN",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Объявления успешно удалены",
                content = @Content(schema = @Schema(implementation = String.class))
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
    @DeleteMapping("/posts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> clearAllPosts() {
        fakeDataGeneratorService.clearAllPosts();
        return ResponseEntity.ok("Все объявления и комментарии удалены");
    }
}
