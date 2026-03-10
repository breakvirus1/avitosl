package com.example.avito.controller;

import com.example.avito.entity.User;
import com.example.avito.request.PostRequest;
import com.example.avito.response.PostResponse;
import com.example.avito.service.PostService;
import com.example.avito.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Объявления", description = "API для управления объявлениями (постами)")
public class PostController {

    private final PostService postService;
    private final UserService userService;

    @Operation(
        summary = "Получение списка всех объявлений",
        description = "Возвращает постраничный список всех объявлений с возможностью сортировки",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Список объявлений успешно получен",
                content = @Content(schema = @Schema(implementation = Page.class))
            )
        }
    )
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            @Parameter(description = "Номер страницы (начиная с 0)", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "0"))
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество элементов на странице", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "20"))
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Поля сортировки в формате 'field,direction' (например: 'createdAt,desc')", in = ParameterIn.QUERY, schema = @Schema(type = "array", defaultValue = "createdAt,desc"))
            @RequestParam(defaultValue = "createdAt,desc") String[] sort
    ) {
        Sort.Order order = new Sort.Order(Sort.Direction.fromString(sort[1]), sort[0]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));
        return ResponseEntity.ok(postService.getAllPosts(pageable));
    }

    @Operation(
        summary = "Поиск объявлений по параметрам",
        description = "Возвращает постраничный список объявлений, отфильтрованных по заданным критериям",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Результаты поиска успешно получены",
                content = @Content(schema = @Schema(implementation = Page.class))
            )
        }
    )
    @GetMapping("/search")
    public ResponseEntity<Page<PostResponse>> searchPosts(
            @Parameter(description = "Заголовок объявления (частичное совпадение)", in = ParameterIn.QUERY)
            @RequestParam(required = false) String title,
            @Parameter(description = "Минимальная цена", in = ParameterIn.QUERY, schema = @Schema(type = "number"))
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Максимальная цена", in = ParameterIn.QUERY, schema = @Schema(type = "number"))
            @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "ID категории", in = ParameterIn.QUERY, schema = @Schema(type = "integer"))
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Номер страницы (начиная с 0)", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "0"))
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество элементов на странице", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "20"))
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Поля сортировки в формате 'field,direction'", in = ParameterIn.QUERY, schema = @Schema(type = "array", defaultValue = "createdAt,desc"))
            @RequestParam(defaultValue = "createdAt,desc") String[] sort
    ) {
        Sort.Order order = new Sort.Order(Sort.Direction.fromString(sort[1]), sort[0]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));
        return ResponseEntity.ok(postService.searchPosts(title, minPrice, maxPrice, categoryId, pageable));
    }

    @Operation(
        summary = "Получение объявления по ID",
        description = "Возвращает детальную информацию об объявлении по его идентификатору",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Объявление успешно найдено",
                content = @Content(schema = @Schema(implementation = PostResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Объявление не найдено"
            )
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(
            @Parameter(description = "ID объявления", in = ParameterIn.PATH, required = true, schema = @Schema(type = "integer"))
            @PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @Operation(
        summary = "Создание нового объявления",
        description = "Создает новое объявление. Требует аутентификации. Автором объявления является текущий пользователь",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Объявление успешно создано",
                content = @Content(schema = @Schema(implementation = PostResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Ошибка валидации данных"
            )
        }
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> createPost(@RequestBody
        @Parameter(description = "Данные объявления", required = true)
        PostRequest request) {
        User author = getCurrentUser();
        return ResponseEntity.ok(postService.createPost(request, author));
    }

    @Operation(
        summary = "Обновление объявления",
        description = "Обновляет существующее объявление. Требует аутентификации. Пользователь может редактировать только свои объявления",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Объявление успешно обновлено",
                content = @Content(schema = @Schema(implementation = PostResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Нет прав на редактирование этого объявления"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Объявление не найдено"
            )
        }
    )
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> updatePost(
            @Parameter(description = "ID объявления", in = ParameterIn.PATH, required = true, schema = @Schema(type = "integer"))
            @PathVariable Long id,
            @RequestBody
            @Parameter(description = "Обновленные данные объявления", required = true)
            PostRequest request) {
        User author = getCurrentUser();
        return ResponseEntity.ok(postService.updatePost(id, request, author));
    }

    @Operation(
        summary = "Удаление объявления",
        description = "Удаляет объявление. Требует аутентификации. Пользователь может удалять только свои объявления",
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "Объявление успешно удалено"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Нет прав на удаление этого объявления"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Объявление не найдено"
            )
        }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "ID объявления", in = ParameterIn.PATH, required = true, schema = @Schema(type = "integer"))
            @PathVariable Long id) {
        User author = getCurrentUser();
        postService.deletePost(id, author);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Получение объявлений пользователя",
        description = "Возвращает постраничный список объявлений конкретного пользователя",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Список объявлений пользователя успешно получен",
                content = @Content(schema = @Schema(implementation = Page.class))
            )
        }
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostResponse>> getPostsByUser(
            @Parameter(description = "ID пользователя", in = ParameterIn.PATH, required = true, schema = @Schema(type = "integer"))
            @PathVariable Long userId,
            @Parameter(description = "Номер страницы (начиная с 0)", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "0"))
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество элементов на странице", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "20"))
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Поля сортировки в формате 'field,direction'", in = ParameterIn.QUERY, schema = @Schema(type = "array", defaultValue = "createdAt,desc"))
            @RequestParam(defaultValue = "createdAt,desc") String[] sort
    ) {
        Sort.Order order = new Sort.Order(Sort.Direction.fromString(sort[1]), sort[0]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));
        return ResponseEntity.ok(postService.getPostsByUser(userId, pageable));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
}