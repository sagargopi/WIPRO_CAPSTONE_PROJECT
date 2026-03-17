package com.example.user.repository;

import com.example.user.modal.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Get all messages for a specific user
     */
    List<Message> findByUserId(Long userId);

    /**
     * Get all messages for a specific owner/admin
     */
    List<Message> findByOwnerId(Long ownerId);

    /**
     * Get conversation between user and owner
     */
    @Query("SELECT m FROM Message m WHERE (m.userId = :userId AND m.ownerId = :ownerId) OR (m.userId = :ownerId AND m.ownerId = :userId) ORDER BY m.createdAt DESC")
    List<Message> getConversation(@Param("userId") Long userId, @Param("ownerId") Long ownerId);

    /**
     * Get unread messages for user
     */
    @Query("SELECT m FROM Message m WHERE m.userId = :userId AND m.isRead = false")
    List<Message> getUnreadMessagesForUser(@Param("userId") Long userId);

    /**
     * Get unread messages for owner
     */
    @Query("SELECT m FROM Message m WHERE m.ownerId = :ownerId AND m.isRead = false")
    List<Message> getUnreadMessagesForOwner(@Param("ownerId") Long ownerId);

    /**
     * Count unread messages for user
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.userId = :userId AND m.isRead = false")
    Long countUnreadMessagesForUser(@Param("userId") Long userId);

    /**
     * Count unread messages for owner
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.ownerId = :ownerId AND m.isRead = false")
    Long countUnreadMessagesForOwner(@Param("ownerId") Long ownerId);

    void deleteByUserId(Long userId);
}
