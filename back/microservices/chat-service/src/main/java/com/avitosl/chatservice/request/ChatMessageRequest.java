package com.avitosl.chatservice.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatMessageRequest {
    @NotBlank
    private String receiverKeycloakId;

    @NotBlank
    private String message;
}
