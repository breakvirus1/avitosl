package com.avitosl.chatservice.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatMessageRequest {
    @NotNull
    private Long senderId;

    @NotNull
    private Long receiverId;

    @NotBlank
    private String message;
}
