package com.example.avito.controller;

import com.example.avito.entity.User;
import com.example.avito.request.PhotoRequest;
import com.example.avito.response.PhotoResponse;
import com.example.avito.security.UserSecurityService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
@Validated
@Tag(name = "Фотографии", description = "API для управления фотографиями объявлений")
public class PhotoController {

    private final PhotoService photoService;
    private final UserSecurityService userSecurityService;

    private User getCurrentUser() {
        return userSecurityService.getCurrentUser();
    }

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
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(photoService.updatePhoto(id, request, currentUser));
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
        User currentUser = getCurrentUser();
        photoService.deletePhoto(id, currentUser);
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
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(photoService.setPrimaryPhoto(id, currentUser));
    }

    @Operation(
        summary = "Загрузка файла",
        description = "Загружает файл на сервер и возвращает URL. Требует аутентификации",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Файл успешно загружен",
                content = @Content(schema = @Schema(implementation = PhotoResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PhotoResponse> uploadFile(
            @Parameter(description = "ID объявления", in = ParameterIn.QUERY, required = true, schema = @Schema(type = "integer"))
            @RequestParam("postId") Long postId,
            @Parameter(description = "Файл для загрузки", required = true)
            @RequestPart("file") MultipartFile file) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(photoService.uploadFile(file, postId, currentUser));
    }

    @Operation(
        summary = "Получение загруженного файла",
        description = "Возвращает файл по указанному URL",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Файл успешно получен"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Файл не найден"
            )
        }
    )
    @GetMapping("/{id}/file")
    @PreAuthorize("permitAll()")
    public ResponseEntity<byte[]> getFile(
            @Parameter(description = "ID фотографии", in = ParameterIn.PATH, required = true, schema = @Schema(type = "integer"))
            @PathVariable Long id) {
        PhotoResponse photo = photoService.getPhotoById(id);
        String relativePath = photo.getUrl().startsWith("/") ? photo.getUrl().substring(1) : photo.getUrl();
        String filePath = System.getProperty("user.dir") + File.separator + relativePath;
        
        try {
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            String contentType = Files.probeContentType(Paths.get(filePath));
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .body(fileBytes);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Файл не найден");
        }
    }
}