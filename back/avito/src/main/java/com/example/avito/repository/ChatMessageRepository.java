package com.example.avito.repository;

import com.example.avito.entity.ChatMessage;
import com.example.avito.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT cm FROM ChatMessage cm WHERE " +
           "(cm.sender.id = :userId1 AND cm.receiver.id = :userId2) OR " +
           "(cm.sender.id = :userId2 AND cm.receiver.id = :userId1) " +
           "ORDER BY cm.createdAt ASC")
    Page<ChatMessage> findConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2, Pageable pageable);

    @Query("SELECT cm FROM ChatMessage cm WHERE " +
           "(cm.sender.id = :userId1 AND cm.receiver.id = :userId2) OR " +
           "(cm.sender.id = :userId2 AND cm.receiver.id = :userId1) " +
           "ORDER BY cm.createdAt DESC")
    List<ChatMessage> findLatestMessages(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT DISTINCT cm.receiver FROM ChatMessage cm WHERE cm.sender.id = :userId")
    List<User> findConversationPartners(@Param("userId") Long userId);

    @Query("SELECT DISTINCT cm.sender FROM ChatMessage cm WHERE cm.receiver.id = :userId")
    List<User> findSenders(@Param("userId") Long userId);

    long countByReceiverIdAndReadFalse(Long receiverId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.receiver.id = :userId AND cm.read = false ORDER BY cm.createdAt DESC")
    List<ChatMessage> findUnreadMessages(@Param("userId") Long userId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.receiver.id = :userId AND cm.read = false ORDER BY cm.createdAt DESC")
    Page<ChatMessage> findUnreadMessages(@Param("userId") Long userId, Pageable pageable);
}