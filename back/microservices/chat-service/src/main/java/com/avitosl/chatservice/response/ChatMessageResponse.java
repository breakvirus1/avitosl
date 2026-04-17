package com.avitosl.chatservice.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private String senderKeycloakId;
    private String receiverKeycloakId;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
