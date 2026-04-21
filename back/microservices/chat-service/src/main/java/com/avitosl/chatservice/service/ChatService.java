package com.avitosl.chatservice.service;

import com.avitosl.chatservice.entity.ChatMessage;
import com.avitosl.chatservice.exception.NotFoundException;
import com.avitosl.chatservice.feign.UserServiceClient;
import com.avitosl.chatservice.mapper.ChatMessageMapper;
import com.avitosl.chatservice.repository.ChatMessageRepository;
import com.avitosl.chatservice.response.UserResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage sendMessage(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    public ChatMessage getMessageById(Long id) {
        return chatMessageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Chat message not found with id: " + id));
    }

    public List<ChatMessage> getMessagesBetweenUsers(String userKeycloakId1, String userKeycloakId2) {
        List<ChatMessage> messages1to2 = chatMessageRepository.findBySenderKeycloakIdAndReceiverKeycloakId(userKeycloakId1, userKeycloakId2);
        List<ChatMessage> messages2to1 = chatMessageRepository.findBySenderKeycloakIdAndReceiverKeycloakId(userKeycloakId2, userKeycloakId1);
        List<ChatMessage> allMessages = new ArrayList<>(messages1to2);
        allMessages.addAll(messages2to1);
        allMessages.sort(Comparator.comparing(ChatMessage::getCreatedAt));
        return allMessages;
    }

    public List<ChatMessage> getMessagesBySender(String senderKeycloakId) {
        return chatMessageRepository.findBySenderKeycloakId(senderKeycloakId);
    }

    public List<ChatMessage> getMessagesByReceiver(String receiverKeycloakId) {
        return chatMessageRepository.findByReceiverKeycloakId(receiverKeycloakId);
    }

    public List<ChatMessage> getUnreadMessages(String receiverKeycloakId) {
        return chatMessageRepository.findByReceiverKeycloakIdAndIsReadFalse(receiverKeycloakId);
    }

    public long countUnreadMessages(String receiverKeycloakId) {
        return chatMessageRepository.countByReceiverKeycloakIdAndIsReadFalse(receiverKeycloakId);
    }

    public ChatMessage markAsRead(Long messageId) {
        ChatMessage message = getMessageById(messageId);
        message.setIsRead(true);
        return chatMessageRepository.save(message);
    }

    public void deleteMessage(Long id) {
        ChatMessage message = getMessageById(id);
        chatMessageRepository.delete(message);
    }

    public void markAllAsRead(String receiverKeycloakId, String senderKeycloakId) {
        List<ChatMessage> unreadMessages = chatMessageRepository
                .findByReceiverKeycloakIdAndIsReadFalse(receiverKeycloakId)
                .stream()
                .filter(msg -> msg.getSenderKeycloakId().equals(senderKeycloakId))
                .collect(Collectors.toList());

        unreadMessages.forEach(msg -> msg.setIsRead(true));
        if (!unreadMessages.isEmpty()) {
            chatMessageRepository.saveAll(unreadMessages);
        }
    }
}
