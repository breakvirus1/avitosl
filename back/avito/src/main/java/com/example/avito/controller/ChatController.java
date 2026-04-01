package com.example.avito.controller;

import com.example.avito.entity.User;
import com.example.avito.request.ChatMessageRequest;
import com.example.avito.response.ChatMessageResponse;
import com.example.avito.security.UserSecurityService;
import com.example.avito.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Validated
@Tag(name = "Чат", description = "API для обмена сообщениями между пользователями")
public class ChatController {

    private final ChatService chatService;
    private final UserSecurityService userSecurityService;

    private User getCurrentUser() {
        return userSecurityService.getCurrentUser();
    }

    @Operation(
        summary = "Отправка сообщения",
        description = "Отправляет сообщение другому пользователю",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Сообщение успешно отправлено",
                content = @Content(schema = @Schema(implementation = ChatMessageResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Неверные данные запроса"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Получатель не найден"
            )
        }
    )
    @PostMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @Valid @RequestBody ChatMessageRequest request
    ) {
        User sender = getCurrentUser();
        ChatMessageResponse response = chatService.sendMessage(request, sender);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Получение истории переписки",
        description = "Возвращает историю сообщений между текущим пользователем и указанным пользователем",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "История переписки успешно получена",
                content = @Content(schema = @Schema(implementation = Page.class))
            )
        }
    )
    @GetMapping("/conversation/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ChatMessageResponse>> getConversation(
            @Parameter(description = "ID пользователя для переписки", in = ParameterIn.PATH, required = true)
            @PathVariable Long userId,
            @Parameter(description = "Номер страницы (начиная с 0)", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "0"))
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество элементов на странице", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "50"))
            @RequestParam(defaultValue = "50") int size
    ) {
        User currentUser = getCurrentUser();
        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));
        Page<ChatMessageResponse> response = chatService.getConversation(userId, currentUser.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Получение количества непрочитанных сообщений",
        description = "Возвращает количество непрочитанных сообщений для текущего пользователя",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Количество непрочитанных сообщений"
            )
        }
    )
    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getUnreadCount() {
        User currentUser = getCurrentUser();
        long count = chatService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(count);
    }

    @Operation(
        summary = "Отметить сообщение как прочитанное",
        description = "Помечает конкретное сообщение как прочитанное",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Сообщение помечено как прочитанное"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Сообщение не найдено"
            )
        }
    )
    @PostMapping("/messages/{messageId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "ID сообщения", in = ParameterIn.PATH, required = true)
            @PathVariable Long messageId
    ) {
        User currentUser = getCurrentUser();
        chatService.markAsRead(messageId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Получение списка непрочитанных сообщений",
        description = "Возвращает список непрочитанных сообщений для текущего пользователя",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Список непрочитанных сообщений",
                content = @Content(schema = @Schema(implementation = Page.class))
            )
        }
    )
    @GetMapping("/unread/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ChatMessageResponse>> getUnreadMessages(
            @Parameter(description = "Номер страницы (начиная с 0)", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "0"))
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество элементов на странице", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "50"))
            @RequestParam(defaultValue = "50") int size
    ) {
        User currentUser = getCurrentUser();
        Sort.Order order = new Sort.Order(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));
        Page<ChatMessageResponse> response = chatService.getUnreadMessages(currentUser.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Отметить все сообщения от отправителя как прочитанные",
        description = "Помечает все сообщения от указанного отправителя как прочитанные",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Сообщения помечены как прочитанные"
            )
        }
    )
    @PostMapping("/messages/read-all/{senderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead(
            @Parameter(description = "ID отправителя", in = ParameterIn.PATH, required = true)
            @PathVariable Long senderId
    ) {
        User currentUser = getCurrentUser();
        chatService.markAllAsRead(currentUser.getId(), senderId);
        return ResponseEntity.ok().build();
    }
}