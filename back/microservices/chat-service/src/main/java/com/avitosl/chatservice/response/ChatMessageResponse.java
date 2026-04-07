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
    private Long senderId;
    private Long receiverId;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
