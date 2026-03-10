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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Категории", description = "API для управления категориями объявлений")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(
        summary = "Получение всех категорий",
        description = "Возвращает полный список всех категорий в иерархической структуре",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Список категорий успешно получен",
                content = @Content(schema = @Schema(implementation = CategoryResponse.class, type = "array"))
            )
        }
    )
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(
        summary = "Получение корневых категорий",
        description = "Возвращает список категорий верхнего уровня (без родительских категорий)",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Корневые категории успешно получены",
                content = @Content(schema = @Schema(implementation = CategoryResponse.class, type = "array"))
            )
        }
    )
    @GetMapping("/root")
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }

    @Operation(
        summary = "Получение подкатегорий",
        description = "Возвращает список дочерних категорий для указанной родительской категории",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Подкатегории успешно получены",
                content = @Content(schema = @Schema(implementation = CategoryResponse.class, type = "array"))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Родительская категория не найдена"
            )
        }
    )
    @GetMapping("/{id}/children")
    public ResponseEntity<List<CategoryResponse>> getChildCategories(
            @Parameter(description = "ID родительской категории", in = ParameterIn.PATH, required = true, schema = @Schema(type = "integer"))
            @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getChildCategories(id));
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
        description = "Создает новую категорию. Доступно только администраторам",
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
            )
        }
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody
        @Parameter(description = "Данные категории", required = true)
        CategoryRequest request) {
        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    @Operation(
        summary = "Обновление категории",
        description = "Обновляет информацию о категории. Доступно только администраторам",
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
            CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @Operation(
        summary = "Удаление категории",
        description = "Удаляет категорию из системы. Доступно только администраторам",
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