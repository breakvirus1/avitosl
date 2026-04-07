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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageMapper chatMessageMapper;
    private final UserServiceClient userServiceClient;

    public ChatMessage sendMessage(ChatMessage chatMessage) {
        // Проверяем существование отправителя
        try {
            userServiceClient.getUserById(chatMessage.getSenderId());
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Sender not found with id: " + chatMessage.getSenderId());
        } catch (FeignException e) {
            throw new RuntimeException("Failed to fetch sender: " + e.getMessage());
        }
        
        // Проверяем существование получателя
        try {
            userServiceClient.getUserById(chatMessage.getReceiverId());
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Receiver not found with id: " + chatMessage.getReceiverId());
        } catch (FeignException e) {
            throw new RuntimeException("Failed to fetch receiver: " + e.getMessage());
        }
        
        return chatMessageRepository.save(chatMessage);
    }

    public ChatMessage getMessageById(Long id) {
        return chatMessageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Chat message not found with id: " + id));
    }

    public List<ChatMessage> getMessagesBetweenUsers(Long userId1, Long userId2) {
        return chatMessageRepository.findBySenderIdAndReceiverId(userId1, userId2);
    }

    public List<ChatMessage> getMessagesBySender(Long senderId) {
        return chatMessageRepository.findBySenderId(senderId);
    }

    public List<ChatMessage> getMessagesByReceiver(Long receiverId) {
        return chatMessageRepository.findByReceiverId(receiverId);
    }

    public List<ChatMessage> getUnreadMessages(Long receiverId) {
        return chatMessageRepository.findByIsReadFalse();
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
}
