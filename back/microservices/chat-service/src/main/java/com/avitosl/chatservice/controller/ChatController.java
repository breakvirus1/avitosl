package com.avitosl.chatservice.controller;

import com.avitosl.chatservice.entity.ChatMessage;
import com.avitosl.chatservice.request.ChatMessageRequest;
import com.avitosl.chatservice.response.ChatMessageResponse;
import com.avitosl.chatservice.service.ChatService;
import com.avitosl.chatservice.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ChatController {

    private final ChatService chatService;
    private final JwtUtil jwtUtil;

    @PostMapping("/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(@RequestHeader(value = "Authorization", required = false) String token, @Valid @RequestBody ChatMessageRequest request) {
        String senderKeycloakId = token != null ? jwtUtil.getUserKeycloakIdFromToken(token) : "test-sender-id";
        ChatMessage message = new ChatMessage();
        message.setSenderKeycloakId(senderKeycloakId);
        message.setReceiverKeycloakId(request.getReceiverKeycloakId());
        message.setMessage(request.getMessage());
        message.setIsRead(false);
        message = chatService.sendMessage(message);
        return ResponseEntity.ok(mapToResponse(message));
    }

    @GetMapping("/message/{messageId}")
    public ResponseEntity<ChatMessageResponse> getMessage(@PathVariable Long messageId) {
        ChatMessage message = chatService.getMessageById(messageId);
        return ResponseEntity.ok(mapToResponse(message));
    }

    @GetMapping("/conversation/{userKeycloakId1}/{userKeycloakId2}")
    public ResponseEntity<List<ChatMessageResponse>> getConversation(@PathVariable String userKeycloakId1, @PathVariable String userKeycloakId2) {
        List<ChatMessage> messages = chatService.getMessagesBetweenUsers(userKeycloakId1, userKeycloakId2);
        List<ChatMessageResponse> responses = messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/sent/{senderKeycloakId}")
    public ResponseEntity<List<ChatMessageResponse>> getSentMessages(@PathVariable String senderKeycloakId) {
        List<ChatMessage> messages = chatService.getMessagesBySender(senderKeycloakId);
        List<ChatMessageResponse> responses = messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/received/{receiverKeycloakId}")
    public ResponseEntity<List<ChatMessageResponse>> getReceivedMessages(@PathVariable String receiverKeycloakId) {
        List<ChatMessage> messages = chatService.getMessagesByReceiver(receiverKeycloakId);
        List<ChatMessageResponse> responses = messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/unread/{receiverKeycloakId}")
    public ResponseEntity<List<ChatMessageResponse>> getUnreadMessages(@PathVariable String receiverKeycloakId) {
        List<ChatMessage> messages = chatService.getUnreadMessages(receiverKeycloakId);
        List<ChatMessageResponse> responses = messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(@RequestHeader(value = "Authorization", required = false) String token) {
        String userKeycloakId = token != null ? jwtUtil.getUserKeycloakIdFromToken(token) : "test-sender-id";
        long count = chatService.countUnreadMessages(userKeycloakId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/unread/messages")
    public ResponseEntity<List<ChatMessageResponse>> getUnreadMessagesByAuth(@RequestHeader(value = "Authorization", required = false) String token) {
        String userKeycloakId = token != null ? jwtUtil.getUserKeycloakIdFromToken(token) : "test-sender-id";
        List<ChatMessage> messages = chatService.getUnreadMessages(userKeycloakId);
        return ResponseEntity.ok(messages.stream().map(this::mapToResponse).toList());
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Chat service is working");
    }

    @PostMapping("/{messageId}/read")
    public ResponseEntity<ChatMessageResponse> markAsRead(@PathVariable Long messageId) {
        ChatMessage message = chatService.markAsRead(messageId);
        return ResponseEntity.ok(mapToResponse(message));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        chatService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/messages/read-all/{senderId}")
    public ResponseEntity<Void> markAllAsRead(@RequestHeader(value = "Authorization", required = false) String token,
                                              @PathVariable String senderId) {
        String userKeycloakId = token != null ? jwtUtil.getUserKeycloakIdFromToken(token) : "test-sender-id";
        chatService.markAllAsRead(userKeycloakId, senderId);
        return ResponseEntity.ok().build();
    }

    private ChatMessageResponse mapToResponse(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getSenderKeycloakId(),
                message.getReceiverKeycloakId(),
                message.getMessage(),
                message.getIsRead(),
                message.getCreatedAt()
        );
    }
}
