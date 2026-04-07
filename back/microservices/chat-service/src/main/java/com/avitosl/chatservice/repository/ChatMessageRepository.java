package com.avitosl.chatservice.repository;

import com.avitosl.chatservice.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySenderId(Long senderId);
    List<ChatMessage> findByReceiverId(Long receiverId);
    List<ChatMessage> findBySenderIdAndReceiverId(Long senderId, Long receiverId);
    List<ChatMessage> findByIsReadFalse();
}
