package com.example.user.repository;

import com.example.user.modal.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Get all notifications for a user
     */
    List<Notification> findByUserId(Long userId);

    /**
     * Get all notifications for an owner/admin
     */
    List<Notification> findByOwnerId(Long ownerId);

    /**
     * Get unread notifications for user
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> getUnreadNotificationsForUser(@Param("userId") Long userId);

    /**
     * Get unread notifications for owner
     */
    @Query("SELECT n FROM Notification n WHERE n.ownerId = :ownerId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> getUnreadNotificationsForOwner(@Param("ownerId") Long ownerId);

    /**
     * Count unread notifications for user
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    Long countUnreadNotificationsForUser(@Param("userId") Long userId);

    /**
     * Count unread notifications for owner
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.ownerId = :ownerId AND n.isRead = false")
    Long countUnreadNotificationsForOwner(@Param("ownerId") Long ownerId);

    /**
     * Get active notifications for user
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isActive = true ORDER BY n.createdAt DESC")
    List<Notification> getActiveNotificationsForUser(@Param("userId") Long userId);

    /**
     * Get notifications by type
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.notificationType = :notificationType ORDER BY n.createdAt DESC")
    List<Notification> getNotificationsByType(@Param("userId") Long userId, @Param("notificationType") String notificationType);

    void deleteByUserId(Long userId);
}
