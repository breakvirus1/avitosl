package com.avitosl.chatservice.service;

import com.avitosl.chatservice.entity.ChatMessage;
import com.avitosl.chatservice.exception.NotFoundException;
import com.avitosl.chatservice.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private ChatService chatService;

    @Captor
    private ArgumentCaptor<String> senderKeycloakIdCaptor;

    @Captor
    private ArgumentCaptor<String> receiverKeycloakIdCaptor;

    private ChatMessage message1;
    private ChatMessage message2;
    private ChatMessage message3;

    @BeforeEach
    void setUp() {
        // Setup test data using constructor
        message1 = new ChatMessage(
            1L,
            "user1",
            "user2",
            "Hello from user1 to user2",
            false,
            LocalDateTime.now()
        );

        message2 = new ChatMessage(
            2L,
            "user2",
            "user1",
            "Reply from user2 to user1",
            false,
            LocalDateTime.now()
        );

        message3 = new ChatMessage(
            3L,
            "user1",
            "user2",
            "Second message from user1",
            true,
            LocalDateTime.now()
        );
    }

    @Test
    void sendMessage_ShouldSaveMessageAndReturnIt() {
        // Given
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setCreatedAt(LocalDateTime.now());
            return saved;
        });

        ChatMessage messageToSend = new ChatMessage();
        messageToSend.setSenderKeycloakId("user1");
        messageToSend.setReceiverKeycloakId("user2");
        messageToSend.setMessage("Test message");

        // When
        ChatMessage result = chatService.sendMessage(messageToSend);

        // Then
        assertNotNull(result);
        assertEquals("user1", result.getSenderKeycloakId());
        assertEquals("user2", result.getReceiverKeycloakId());
        assertEquals("Test message", result.getMessage());
        assertFalse(result.getIsRead());
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
    }

    @Test
    void getMessageById_ShouldReturnMessage_WhenExists() {
        // Given
        when(chatMessageRepository.findById(1L)).thenReturn(Optional.of(message1));

        // When
        ChatMessage result = chatService.getMessageById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("user1", result.getSenderKeycloakId());
        verify(chatMessageRepository, times(1)).findById(1L);
    }

    @Test
    void getMessageById_ShouldThrowNotFoundException_WhenNotExists() {
        // Given
        when(chatMessageRepository.findById(999L)).thenReturn(Optional.empty());

        // Then/When
        assertThrows(NotFoundException.class, () -> {
            chatService.getMessageById(999L);
        });
        verify(chatMessageRepository, times(1)).findById(999L);
    }

    @Test
    void getMessagesBetweenUsers_ShouldReturnOnlyMessagesFromUser1ToUser2() {
        // Given
        when(chatMessageRepository.findBySenderKeycloakIdAndReceiverKeycloakId("user1", "user2"))
                .thenReturn(Arrays.asList(message1, message3));

        // When
        List<ChatMessage> result = chatService.getMessagesBetweenUsers("user1", "user2");

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(message1));
        assertTrue(result.contains(message3));
        verify(chatMessageRepository, times(1))
                .findBySenderKeycloakIdAndReceiverKeycloakId("user1", "user2");
    }

    @Test
    void getMessagesBySender_ShouldReturnMessagesSentByUser() {
        // Given
        when(chatMessageRepository.findBySenderKeycloakId("user1"))
                .thenReturn(Arrays.asList(message1, message3));

        // When
        List<ChatMessage> result = chatService.getMessagesBySender("user1");

        // Then
        assertEquals(2, result.size());
        verify(chatMessageRepository, times(1)).findBySenderKeycloakId("user1");
    }

    @Test
    void getMessagesByReceiver_ShouldReturnMessagesReceivedByUser() {
        // Given
        when(chatMessageRepository.findByReceiverKeycloakId("user1"))
                .thenReturn(Arrays.asList(message2));

        // When
        List<ChatMessage> result = chatService.getMessagesByReceiver("user1");

        // Then
        assertEquals(1, result.size());
        assertTrue(result.contains(message2));
        verify(chatMessageRepository, times(1)).findByReceiverKeycloakId("user1");
    }

    @Test
    void getUnreadMessages_ShouldReturnUnreadMessagesForReceiver() {
        // Given
        when(chatMessageRepository.findByReceiverKeycloakIdAndIsReadFalse("user1"))
                .thenReturn(Arrays.asList(message2));

        // When
        List<ChatMessage> result = chatService.getUnreadMessages("user1");

        // Then
        assertEquals(1, result.size());
        assertTrue(result.contains(message2));
        assertFalse(result.get(0).getIsRead());
        verify(chatMessageRepository, times(1))
                .findByReceiverKeycloakIdAndIsReadFalse("user1");
    }

    @Test
    void countUnreadMessages_ShouldReturnCountOfUnreadMessages() {
        // Given
        when(chatMessageRepository.countByReceiverKeycloakIdAndIsReadFalse("user1"))
                .thenReturn(2L);

        // When
        long count = chatService.countUnreadMessages("user1");

        // Then
        assertEquals(2L, count);
        verify(chatMessageRepository, times(1))
                .countByReceiverKeycloakIdAndIsReadFalse("user1");
    }

    @Test
    void markAsRead_ShouldSetMessageAsReadAndSave() {
        // Given
        when(chatMessageRepository.findById(1L)).thenReturn(Optional.of(message1));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ChatMessage result = chatService.markAsRead(1L);

        // Then
        assertTrue(result.getIsRead());
        verify(chatMessageRepository, times(1)).findById(1L);
        verify(chatMessageRepository, times(1)).save(message1);
    }

    @Test
    void markAsRead_ShouldThrowException_WhenMessageNotFound() {
        // Given
        when(chatMessageRepository.findById(999L)).thenReturn(Optional.empty());

        // Then/When
        assertThrows(NotFoundException.class, () -> {
            chatService.markAsRead(999L);
        });
        verify(chatMessageRepository, times(1)).findById(999L);
        verify(chatMessageRepository, never()).save(any());
    }

    @Test
    void deleteMessage_ShouldDeleteMessage_WhenExists() {
        // Given
        when(chatMessageRepository.findById(1L)).thenReturn(Optional.of(message1));
        doNothing().when(chatMessageRepository).delete(any(ChatMessage.class));

        // When
        chatService.deleteMessage(1L);

        // Then
        verify(chatMessageRepository, times(1)).findById(1L);
        verify(chatMessageRepository, times(1)).delete(message1);
    }

    @Test
    void deleteMessage_ShouldThrowException_WhenMessageNotFound() {
        // Given
        when(chatMessageRepository.findById(999L)).thenReturn(Optional.empty());

        // Then/When
        assertThrows(NotFoundException.class, () -> {
            chatService.deleteMessage(999L);
        });
        verify(chatMessageRepository, times(1)).findById(999L);
        verify(chatMessageRepository, never()).delete(any());
    }

    @Test
    void getConversation_ShouldReturnAllMessagesBidirectionalSorted() {
        // Given - messages with specific timestamps to test sorting
        LocalDateTime now = LocalDateTime.now();
        ChatMessage msg1 = new ChatMessage(1L, "user1", "user2", "First message", false, now.minusMinutes(10));
        ChatMessage msg2 = new ChatMessage(2L, "user2", "user1", "Second message", false, now.minusMinutes(5));
        ChatMessage msg3 = new ChatMessage(3L, "user1", "user2", "Third message", true, now.minusMinutes(1));

        when(chatMessageRepository.findBySenderKeycloakIdAndReceiverKeycloakId("user1", "user2"))
                .thenReturn(Arrays.asList(msg1, msg3));
        when(chatMessageRepository.findBySenderKeycloakIdAndReceiverKeycloakId("user2", "user1"))
                .thenReturn(Arrays.asList(msg2));

        // When
        List<ChatMessage> result = chatService.getMessagesBetweenUsers("user1", "user2");

        // Then - should contain all three messages in chronological order
        assertEquals(3, result.size());
        assertTrue(result.contains(msg1));
        assertTrue(result.contains(msg2));
        assertTrue(result.contains(msg3));
        // Check ordering
        assertEquals(msg1, result.get(0));
        assertEquals(msg2, result.get(1));
        assertEquals(msg3, result.get(2));

        // Verify both repository calls
        verify(chatMessageRepository, times(1))
                .findBySenderKeycloakIdAndReceiverKeycloakId("user1", "user2");
        verify(chatMessageRepository, times(1))
                .findBySenderKeycloakIdAndReceiverKeycloakId("user2", "user1");
    }

    @Test
    void markAllAsRead_ShouldMarkAllUnreadMessagesFromSenderAsRead() {
        // Given - unread messages from two senders: user2 and user3
        LocalDateTime now = LocalDateTime.now();
        ChatMessage unreadFromUser2 = new ChatMessage(1L, "user2", "user1", "Unread msg1", false, now);
        ChatMessage unreadFromUser3 = new ChatMessage(2L, "user3", "user1", "Unread msg2", false, now);

        when(chatMessageRepository.findByReceiverKeycloakIdAndIsReadFalse("user1"))
                .thenReturn(Arrays.asList(unreadFromUser2, unreadFromUser3));

        // When - mark all as read from user2
        chatService.markAllAsRead("user1", "user2");

        // Then - verify that only message from user2 is marked as read and saved
        assertTrue(unreadFromUser2.getIsRead());
        assertFalse(unreadFromUser3.getIsRead()); // still unread

        verify(chatMessageRepository, times(1)).findByReceiverKeycloakIdAndIsReadFalse("user1");
        verify(chatMessageRepository, times(1)).saveAll(Arrays.asList(unreadFromUser2));
        // Verify that saveAll was not called with messages from user3
        verify(chatMessageRepository, never()).save(unreadFromUser3);
    }

    @Test
    void markAllAsRead_ShouldDoNothingWhenNoUnreadMessages() {
        // Given - no unread messages
        when(chatMessageRepository.findByReceiverKeycloakIdAndIsReadFalse("user1"))
                .thenReturn(Collections.emptyList());

        // When
        chatService.markAllAsRead("user1", "user2");

        // Then - no save should occur
        verify(chatMessageRepository, times(1)).findByReceiverKeycloakIdAndIsReadFalse("user1");
        verify(chatMessageRepository, never()).saveAll(anyList());
    }
}
