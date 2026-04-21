package com.avitosl.chatservice.controller;

import com.avitosl.chatservice.entity.ChatMessage;
import com.avitosl.chatservice.request.ChatMessageRequest;
import com.avitosl.chatservice.response.ChatMessageResponse;
import com.avitosl.chatservice.service.ChatService;
import com.avitosl.chatservice.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.avitosl.chatservice.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ChatController.class)
@Import(SecurityConfig.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void sendMessage_ShouldReturnChatMessageResponse() throws Exception {
        // Given
        ChatMessageRequest request = new ChatMessageRequest();
        request.setReceiverKeycloakId("receiver-123");
        request.setMessage("Hello!");

        ChatMessage message = new ChatMessage();
        message.setId(1L);
        message.setSenderKeycloakId("sender-123");
        message.setReceiverKeycloakId("receiver-123");
        message.setMessage("Hello!");
        message.setIsRead(false);
        message.setCreatedAt(LocalDateTime.now());

        ChatMessageResponse response = new ChatMessageResponse(
                1L, "sender-123", "receiver-123", "Hello!", false, message.getCreatedAt()
        );

        when(jwtUtil.getUserKeycloakIdFromToken("Bearer token")).thenReturn("sender-123");
        when(chatService.sendMessage(any(ChatMessage.class))).thenReturn(message);

        // When/Then
        mockMvc.perform(post("/api/chat/messages")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello!"))
                .andExpect(jsonPath("$.senderKeycloakId").value("sender-123"));
    }

    @Test
    void getMessage_ShouldReturnMessage() throws Exception {
        // Given
        ChatMessage message = new ChatMessage();
        message.setId(1L);
        message.setSenderKeycloakId("sender");
        message.setReceiverKeycloakId("receiver");
        message.setMessage("Test");
        message.setIsRead(false);
        message.setCreatedAt(LocalDateTime.now());

        ChatMessageResponse response = new ChatMessageResponse(
                1L, "sender", "receiver", "Test", false, message.getCreatedAt()
        );

        when(chatService.getMessageById(1L)).thenReturn(message);

        // When/Then
        mockMvc.perform(get("/api/chat/message/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getConversation_ShouldReturnMessages() throws Exception {
        // Given
        ChatMessage msg1 = new ChatMessage();
        msg1.setId(1L);
        msg1.setSenderKeycloakId("user1");
        msg1.setReceiverKeycloakId("user2");
        msg1.setMessage("Hi");
        msg1.setCreatedAt(LocalDateTime.now());

        ChatMessage msg2 = new ChatMessage();
        msg2.setId(2L);
        msg2.setSenderKeycloakId("user2");
        msg2.setReceiverKeycloakId("user1");
        msg2.setMessage("Hello back");
        msg2.setCreatedAt(LocalDateTime.now());

        when(chatService.getMessagesBetweenUsers("user1", "user2")).thenReturn(Arrays.asList(msg1, msg2));

        // When/Then
        mockMvc.perform(get("/api/chat/conversation/user1/user2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Hi"))
                .andExpect(jsonPath("$[1].message").value("Hello back"));
    }

    @Test
    void getSentMessages_ShouldReturnList() throws Exception {
        // Given
        ChatMessage msg = new ChatMessage();
        msg.setId(1L);
        msg.setSenderKeycloakId("user1");
        msg.setReceiverKeycloakId("user2");
        msg.setMessage("Sent");
        msg.setCreatedAt(LocalDateTime.now());

        when(chatService.getMessagesBySender("user1")).thenReturn(Arrays.asList(msg));

        // When/Then
        mockMvc.perform(get("/api/chat/sent/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].senderKeycloakId").value("user1"));
    }

    @Test
    void getReceivedMessages_ShouldReturnList() throws Exception {
        // Given
        ChatMessage msg = new ChatMessage();
        msg.setId(1L);
        msg.setSenderKeycloakId("user1");
        msg.setReceiverKeycloakId("user2");
        msg.setMessage("Received");
        msg.setCreatedAt(LocalDateTime.now());

        when(chatService.getMessagesByReceiver("user2")).thenReturn(Arrays.asList(msg));

        // When/Then
        mockMvc.perform(get("/api/chat/received/user2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].receiverKeycloakId").value("user2"));
    }

    @Test
    void getUnreadMessages_ShouldReturnUnreadOnly() throws Exception {
        // Given
        ChatMessage unread = new ChatMessage();
        unread.setId(1L);
        unread.setSenderKeycloakId("user1");
        unread.setReceiverKeycloakId("user2");
        unread.setMessage("Unread");
        unread.setIsRead(false);
        unread.setCreatedAt(LocalDateTime.now());

        when(chatService.getUnreadMessages("user2")).thenReturn(Arrays.asList(unread));

        // When/Then
        mockMvc.perform(get("/api/chat/unread/user2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isRead").value(false));
    }

    @Test
    void getUnreadCount_ShouldReturnCount() throws Exception {
        // Given
        when(jwtUtil.getUserKeycloakIdFromToken("Bearer token")).thenReturn("user2");
        when(chatService.countUnreadMessages("user2")).thenReturn(5L);

        // When/Then
        mockMvc.perform(get("/api/chat/unread/count")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));
    }

    @Test
    void markAsRead_ShouldMarkMessageRead() throws Exception {
        // Given
        ChatMessage message = new ChatMessage();
        message.setId(1L);
        message.setSenderKeycloakId("sender");
        message.setReceiverKeycloakId("receiver");
        message.setMessage("Test");
        message.setIsRead(true);
        message.setCreatedAt(LocalDateTime.now());

        when(chatService.markAsRead(1L)).thenReturn(message);

        // When/Then
        mockMvc.perform(post("/api/chat/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRead").value(true));
    }

    @Test
    void deleteMessage_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(chatService).deleteMessage(1L);

        // When/Then
        mockMvc.perform(delete("/api/chat/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void markAllAsRead_ShouldReturnOk() throws Exception {
        // Given
        when(jwtUtil.getUserKeycloakIdFromToken("Bearer token")).thenReturn("user2");
        doNothing().when(chatService).markAllAsRead("user2", "sender-123");

        // When/Then
        mockMvc.perform(post("/api/chat/messages/read-all/sender-123")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    void testEndpoint_ShouldReturnWorkingMessage() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/chat/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Chat service is working"));
    }
}
