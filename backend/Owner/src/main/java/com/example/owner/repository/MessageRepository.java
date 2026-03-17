package com.example.owner.repository;

import com.example.owner.modal.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Message entity in Owner microservice
 * Provides data access operations for admin-customer messaging
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Get all messages received by an admin/owner
     */
    List<Message> findByReceiverId(Long receiverId);

    /**
     * Get conversation between two users
     */
    @Query("SELECT m FROM Message m WHERE (m.senderId = :userId AND m.receiverId = :adminId) OR (m.senderId = :adminId AND m.receiverId = :userId) ORDER BY m.createdAt DESC")
    List<Message> getConversation(@Param("userId") Long userId, @Param("adminId") Long adminId);

    /**
     * Get unread messages for admin
     */
    @Query("SELECT m FROM Message m WHERE m.receiverId = :adminId AND m.isRead = false ORDER BY m.createdAt DESC")
    List<Message> getUnreadMessagesForAdmin(@Param("adminId") Long adminId);

    /**
     * Count unread messages for admin
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiverId = :adminId AND m.isRead = false")
    Long countUnreadMessagesForAdmin(@Param("adminId") Long adminId);

    void deleteBySenderIdOrReceiverId(Long userId, Long userId2);
}
