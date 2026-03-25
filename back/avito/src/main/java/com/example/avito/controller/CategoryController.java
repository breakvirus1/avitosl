package com.example.avito.controller;

import com.example.avito.request.CategoryRequest;
import com.example.avito.response.CategoryResponse;
import com.example.avito.service.CategoryService;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Validated
@Tag(name = "Категории", description = "API для управления категориями объявлений")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(
        summary = "Получение всех категорий",
        description = "Возвращает полный список всех категорий с их подкатегориями",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Список категорий успешно получен",
                content = @Content(schema = @Schema(implementation = CategoryResponse.class, type = "array"))
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(
        summary = "Получение категории по ID",
        description = "Возвращает информацию о категории по ее идентификатору",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Категория успешно найдена",
                content = @Content(schema = @Schema(implementation = CategoryResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Категория не найдена"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "ID категории", in = ParameterIn.PATH, required = true, schema = @Schema(type = "integer"))
            @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Operation(
        summary = "Создание новой категории",
        description = "Создает новую категорию с возможностью добавления подкатегорий. Доступно только администраторам",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Категория успешно создана",
                content = @Content(schema = @Schema(implementation = CategoryResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Ошибка валидации данных"
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
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody
        @Parameter(description = "Данные категории (с возможностью указания подкатегорий)", required = true)
        @Valid CategoryRequest request) {
        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    @Operation(
        summary = "Обновление категории",
        description = "Обновляет информацию о категории и ее подкатегорий. Доступно только администраторам",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Категория успешно обновлена",
                content = @Content(schema = @Schema(implementation = CategoryResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Ошибка валидации данных"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Доступ запрещен. Требуется роль администратора"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Категория не найдена"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "ID категории", in = ParameterIn.PATH, required = true, schema = @Schema(type = "integer"))
            @PathVariable Long id,
            @RequestBody
            @Parameter(description = "Обновленные данные категории", required = true)
            @Valid CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @Operation(
        summary = "Удаление категории",
        description = "Удаляет категорию и все ее подкатегории из системы. Доступно только администраторам",
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "Категория успешно удалена"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Доступ запрещен. Требуется роль администратора"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Категория не найдена"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID категории", in = ParameterIn.PATH, required = true, schema = @Schema(type = "integer"))
            @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}