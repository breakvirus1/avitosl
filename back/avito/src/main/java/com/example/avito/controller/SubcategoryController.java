package com.example.avito.controller;

import com.example.avito.request.SubcategoryRequest;
import com.example.avito.response.SubcategoryResponse;
import com.example.avito.service.SubcategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/subcategories")
@RequiredArgsConstructor
@Validated
@Tag(name = "Подкатегории", description = "API для управления подкатегориями")
public class SubcategoryController {

    private final SubcategoryService subcategoryService;

    @Operation(
        summary = "Создание подкатегории",
        description = "Создает новую подкатегорию для указанной категории",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Подкатегория успешно создана",
                content = @Content(schema = @Schema(implementation = SubcategoryResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Ошибка валидации"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Категория не найдена"
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Подкатегория с таким названием уже существует"
            )
        }
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubcategoryResponse> createSubcategory(@RequestBody
        @Parameter(description = "Данные для создания подкатегории", required = true)
        @Valid SubcategoryRequest request) {
        return ResponseEntity.ok(subcategoryService.createSubcategory(request));
    }

    @Operation(
        summary = "Получение подкатегории по ID",
        description = "Возвращает информацию о подкатегории по ее идентификатору",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Подкатегория найдена",
                content = @Content(schema = @Schema(implementation = SubcategoryResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Подкатегория не найдена"
            )
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<SubcategoryResponse> getSubcategoryById(
            @Parameter(description = "ID подкатегории", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(subcategoryService.getSubcategoryById(id));
    }

    @Operation(
        summary = "Получение всех подкатегорий",
        description = "Возвращает список всех подкатегорий",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Список подкатегорий успешно получен",
                content = @Content(schema = @Schema(implementation = List.class))
            )
        }
    )
    @GetMapping
    public ResponseEntity<List<SubcategoryResponse>> getAllSubcategories() {
        return ResponseEntity.ok(subcategoryService.getAllSubcategories());
    }

    @Operation(
        summary = "Получение подкатегорий по категории",
        description = "Возвращает список всех подкатегорий для указанной категории",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Список подкатегорий успешно получен",
                content = @Content(schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Категория не найдена"
            )
        }
    )
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<SubcategoryResponse>> getSubcategoriesByCategoryId(
            @Parameter(description = "ID категории", required = true) @PathVariable Long categoryId) {
        return ResponseEntity.ok(subcategoryService.getSubcategoriesByCategoryId(categoryId));
    }

    @Operation(
        summary = "Обновление подкатегории",
        description = "Обновляет информацию о подкатегории",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Подкатегория успешно обновлена",
                content = @Content(schema = @Schema(implementation = SubcategoryResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Ошибка валидации"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Подкатегория или категория не найдена"
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Подкатегория с таким названием уже существует"
            )
        }
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubcategoryResponse> updateSubcategory(
            @Parameter(description = "ID подкатегории", required = true) @PathVariable Long id,
            @RequestBody
            @Parameter(description = "Данные для обновления подкатегории", required = true)
            @Valid SubcategoryRequest request) {
        return ResponseEntity.ok(subcategoryService.updateSubcategory(id, request));
    }

    @Operation(
        summary = "Удаление подкатегории",
        description = "Удаляет подкатегорию по ее идентификатору",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Подкатегория успешно удалена"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Подкатегория не найдена"
            )
        }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSubcategory(
            @Parameter(description = "ID подкатегории", required = true) @PathVariable Long id) {
        subcategoryService.deleteSubcategory(id);
        return ResponseEntity.ok().build();
    }
}
