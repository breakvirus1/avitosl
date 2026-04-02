package com.example.avito.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRequest {
    @NotNull(message = "ID получателя обязателен")
    private Long receiverId;

    @NotNull(message = "ID поста обязателен")
    private Long postId;

    @NotBlank(message = "Сообщение не может быть пустым")
    private String message;
}