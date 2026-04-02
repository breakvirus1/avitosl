package com.example.avito.response;

import com.example.avito.entity.ChatMessage;
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
    private Long senderId;
    private String senderFirstName;
    private Long receiverId;
    private String receiverFirstName;
    private Long postId;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
}