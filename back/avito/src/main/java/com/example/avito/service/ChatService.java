package com.example.avito.service;

import com.example.avito.entity.ChatMessage;
import com.example.avito.entity.Post;
import com.example.avito.entity.User;
import com.example.avito.exception.NotFoundException;
import com.example.avito.repository.ChatMessageRepository;
import com.example.avito.repository.PostRepository;
import com.example.avito.repository.UserRepository;
import com.example.avito.request.ChatMessageRequest;
import com.example.avito.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public ChatMessageResponse sendMessage(ChatMessageRequest request, User sender) {
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new NotFoundException("Получатель не найден"));

        Post post = null;
        if (request.getPostId() != null) {
            post = postRepository.findById(request.getPostId())
                    .orElse(null);
        }

        ChatMessage chatMessage = ChatMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .post(post)
                .message(request.getMessage())
                .read(false)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        return mapToResponse(savedMessage);
    }

    public Page<ChatMessageResponse> getConversation(Long otherUserId, Long currentUserId, Pageable pageable) {
        return chatMessageRepository.findConversation(currentUserId, otherUserId, pageable)
                .map(this::mapToResponse);
    }

    public List<ChatMessageResponse> getLatestMessages(Long userId1, Long userId2) {
        return chatMessageRepository.findLatestMessages(userId1, userId2).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ChatMessageResponse> getUnreadMessages(Long userId) {
        return chatMessageRepository.findUnreadMessages(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<ChatMessageResponse> getUnreadMessages(Long userId, Pageable pageable) {
        return chatMessageRepository.findUnreadMessages(userId, pageable)
                .map(this::mapToResponse);
    }

    public long getUnreadCount(Long userId) {
        return chatMessageRepository.countByReceiverIdAndReadFalse(userId);
    }

    public void markAsRead(Long messageId, Long userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Сообщение не найдено"));

        if (!message.getReceiver().getId().equals(userId)) {
            throw new NotFoundException("Сообщение не найдено");
        }

        if (!message.isRead()) {
            message.setRead(true);
            chatMessageRepository.save(message);
        }
    }

    public void markAllAsRead(Long userId, Long senderId) {
        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadMessages(userId).stream()
                .filter(msg -> msg.getSender().getId().equals(senderId))
                .collect(Collectors.toList());

        unreadMessages.forEach(msg -> msg.setRead(true));
        chatMessageRepository.saveAll(unreadMessages);
    }

    private ChatMessageResponse mapToResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderFirstName(message.getSender().getFirstName())
                .receiverId(message.getReceiver().getId())
                .receiverFirstName(message.getReceiver().getFirstName())
                .postId(message.getPost() != null ? message.getPost().getId() : null)
                .message(message.getMessage())
                .read(message.isRead())
                .createdAt(message.getCreatedAt())
                .build();
    }
}