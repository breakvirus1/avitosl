package com.avitosl.chatservice.controller;

import com.avitosl.chatservice.entity.ChatMessage;
import com.avitosl.chatservice.request.ChatMessageRequest;
import com.avitosl.chatservice.response.ChatMessageResponse;
import com.avitosl.chatservice.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponse> sendMessage(@Valid @RequestBody ChatMessageRequest request) {
        ChatMessage message = chatService.sendMessage(
                new ChatMessage(null, request.getSenderId(), request.getReceiverId(), request.getMessage(), false, null)
        );
        return ResponseEntity.ok(mapToResponse(message));
    }

    @GetMapping("/message/{messageId}")
    public ResponseEntity<ChatMessageResponse> getMessage(@PathVariable Long messageId) {
        ChatMessage message = chatService.getMessageById(messageId);
        return ResponseEntity.ok(mapToResponse(message));
    }

    @GetMapping("/conversation/{userId1}/{userId2}")
    public ResponseEntity<List<ChatMessageResponse>> getConversation(@PathVariable Long userId1, @PathVariable Long userId2) {
        List<ChatMessage> messages = chatService.getMessagesBetweenUsers(userId1, userId2);
        List<ChatMessageResponse> responses = messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/sent/{senderId}")
    public ResponseEntity<List<ChatMessageResponse>> getSentMessages(@PathVariable Long senderId) {
        List<ChatMessage> messages = chatService.getMessagesBySender(senderId);
        List<ChatMessageResponse> responses = messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/received/{receiverId}")
    public ResponseEntity<List<ChatMessageResponse>> getReceivedMessages(@PathVariable Long receiverId) {
        List<ChatMessage> messages = chatService.getMessagesByReceiver(receiverId);
        List<ChatMessageResponse> responses = messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/unread/{receiverId}")
    public ResponseEntity<List<ChatMessageResponse>> getUnreadMessages(@PathVariable Long receiverId) {
        List<ChatMessage> messages = chatService.getUnreadMessages(receiverId);
        List<ChatMessageResponse> responses = messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{messageId}/read")
    public ResponseEntity<ChatMessageResponse> markAsRead(@PathVariable Long messageId) {
        ChatMessage message = chatService.markAsRead(messageId);
        return ResponseEntity.ok(mapToResponse(message));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        chatService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }

    private ChatMessageResponse mapToResponse(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getSenderId(),
                message.getReceiverId(),
                message.getMessage(),
                message.getIsRead(),
                message.getCreatedAt()
        );
    }
}
