package com.avitosl.postservice.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatMessageRequest {
    @NotBlank
    private String receiverKeycloakId;

    @NotBlank
    private String message;
}
