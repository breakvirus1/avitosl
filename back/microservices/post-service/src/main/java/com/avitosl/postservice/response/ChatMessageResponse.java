package com.avitosl.postservice.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {
    private Long id;
    private String senderKeycloakId;
    private String receiverKeycloakId;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
