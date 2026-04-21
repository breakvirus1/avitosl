package com.avitosl.chatservice.repository;

import com.avitosl.chatservice.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySenderKeycloakId(String senderKeycloakId);
    List<ChatMessage> findByReceiverKeycloakId(String receiverKeycloakId);
    List<ChatMessage> findBySenderKeycloakIdAndReceiverKeycloakId(String senderKeycloakId, String receiverKeycloakId);
    List<ChatMessage> findByReceiverKeycloakIdAndIsReadFalse(String receiverKeycloakId);
    long countByReceiverKeycloakIdAndIsReadFalse(String receiverKeycloakId);
}
