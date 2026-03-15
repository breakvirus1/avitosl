package com.example.avito.controller;

import com.example.avito.request.PhotoRequest;
import com.example.avito.response.PhotoResponse;
import com.example.avito.service.PhotoService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
@Validated
@Tag(name = "Фотографии", description = "API для управления фотографиями объявлений")
public class PhotoController {

    private final PhotoService photoService;

    @Operation(
        summary = "Получение фотографий объявления",
        description = "Возвращает список всех фотографий, прикрепленных к указанному объявлению",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Фотографии успешно получены",
                content = @Content(schema = @Schema(implementation = PhotoResponse.class, type = "array"))
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<PhotoResponse>> getPhotosByPost(
            @Parameter(description = "ID объявления", in = ParameterIn.PATH, required = true, schema = @Schema(type = "integer"))
            @PathVariable Long postId) {
        return ResponseEntity.ok(photoService.getPhotosByPost(postId));
    }

    @Operation(
        summary = "Получение фотографии по ID",
        description = "Возвращает информацию о фотографии по ее идентификатору",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Фотография успешно найдена",
                content = @Content(schema = @Schema(implementation = PhotoResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Фотография не найдена"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<PhotoResponse> getPhotoById(
            @Parameter(description = "ID фотографии", in = ParameterIn.PATH, required = true, schema = @Schema(type = "integer"))
            @PathVariable Long id) {
        return ResponseEntity.ok(photoService.getPhotoById(id));
    }

    @Operation(
        summary = "Добавление фотографии",
        description = "Добавляет новую фотографию к объявлению. Требует аутентификации",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Фотография успешно добавлена",
                content = @Content(schema = @Schema(implementation = PhotoResponse.class))
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
    public ResponseEntity<PhotoResponse> addPhoto(@RequestBody
        @Parameter(description = "Данные фотографии", required = true)
        @Valid PhotoRequest request) {
        return ResponseEntity.ok(photoService.addPhoto(request));
    }

    @Operation(
        summary = "Обновление фотографии",
        description = "Обновляет информацию о фотографии. Требует аутентификации",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Фотография успешно обновлена",
                content = @Content(schema = @Schema(implementation = PhotoResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Нет прав на редактирование этой фотографии"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Фотография не найдена"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PhotoResponse> updatePhoto(
            @Parameter(description = "ID фотографии", in = ParameterIn.PATH, required = true, schema = @Schema(type = "integer"))
            @PathVariable Long id,
            @RequestBody
            @Parameter(description = "Обновленные данные фотографии", required = true)
            @Valid PhotoRequest request) {
        return ResponseEntity.ok(photoService.updatePhoto(id, request));
    }

    @Operation(
        summary = "Удаление фотографии",
        description = "Удаляет фотографию. Требует аутентификации",
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "Фотография успешно удалена"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Нет прав на удаление этой фотографии"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Фотография не найдена"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePhoto(
            @Parameter(description = "ID фотографии", in = ParameterIn.PATH, required = true, schema = @Schema(type = "integer"))
            @PathVariable Long id) {
        photoService.deletePhoto(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Установка фотографии как основной",
        description = "Помечает указанную фотографию как основную для объявления. Требует аутентификации",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Фотография успешно установлена как основная",
                content = @Content(schema = @Schema(implementation = PhotoResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Нет прав на изменение этой фотографии"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Фотография не найдена"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @PutMapping("/{id}/set-primary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PhotoResponse> setPrimaryPhoto(
            @Parameter(description = "ID фотографии", in = ParameterIn.PATH, required = true, schema = @Schema(type = "integer"))
            @PathVariable Long id) {
        return ResponseEntity.ok(photoService.setPrimaryPhoto(id));
    }
}