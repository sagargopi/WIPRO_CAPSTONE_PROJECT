package com.example.user.service;

import com.example.user.dto.NotificationDTO;
import com.example.user.modal.Notification;
import com.example.user.modal.NotificationType;
import com.example.user.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling notifications
 * Includes notification creation, retrieval, and marking as read
 * Handles alerts for balance reaching zero, loan status updates, transactions, etc.
 */
@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Create a balance alert notification
     */
    public NotificationDTO createBalanceAlert(Long userId, Long ownerId, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setOwnerId(ownerId);
        notification.setNotificationContent(message);
        notification.setNotificationType(NotificationType.BALANCE_ALERT);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);
        notification.setIsActive(true);

        Notification savedNotification = notificationRepository.save(notification);
        return convertToDTO(savedNotification);
    }

    public void saveNotification(Notification notification) {
        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(LocalDateTime.now());
        }
        notificationRepository.save(notification);
    }

    /**
     * Create a loan status notification
     */
    public NotificationDTO createLoanStatusNotification(Long userId, Long ownerId, String message, Long loanId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setOwnerId(ownerId);
        notification.setNotificationContent(message);
        notification.setNotificationType(NotificationType.LOAN_STATUS);
        notification.setRelatedEntityType("LOAN");
        notification.setRelatedEntityId(loanId);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);
        notification.setIsActive(true);

        Notification savedNotification = notificationRepository.save(notification);
        return convertToDTO(savedNotification);
    }

    /**
     * Create a transaction notification
     */
    public NotificationDTO createTransactionNotification(Long userId, Long ownerId, String message, Long transactionId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setOwnerId(ownerId);
        notification.setNotificationContent(message);
        notification.setNotificationType(NotificationType.TRANSACTION);
        notification.setRelatedEntityType("TRANSACTION");
        notification.setRelatedEntityId(transactionId);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);
        notification.setIsActive(true);

        Notification savedNotification = notificationRepository.save(notification);
        return convertToDTO(savedNotification);
    }

    /**
     * Create a system notification
     */
    public NotificationDTO createSystemNotification(Long userId, Long ownerId, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setOwnerId(ownerId);
        notification.setNotificationContent(message);
        notification.setNotificationType(NotificationType.SYSTEM);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);
        notification.setIsActive(true);

        Notification savedNotification = notificationRepository.save(notification);
        return convertToDTO(savedNotification);
    }

    /**
     * Get all notifications for a user
     */
    public List<NotificationDTO> getNotificationsForUser(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserId(userId);
        return notifications.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Get all notifications for an owner/admin
     */
    public List<NotificationDTO> getNotificationsForOwner(Long ownerId) {
        List<Notification> notifications = notificationRepository.findByOwnerId(ownerId);
        return notifications.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Get unread notifications for user
     */
    public List<NotificationDTO> getUnreadNotificationsForUser(Long userId) {
        List<Notification> notifications = notificationRepository.getUnreadNotificationsForUser(userId);
        return notifications.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Get unread notifications for owner
     */
    public List<NotificationDTO> getUnreadNotificationsForOwner(Long ownerId) {
        List<Notification> notifications = notificationRepository.getUnreadNotificationsForOwner(ownerId);
        return notifications.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Count unread notifications for user
     */
    public Long countUnreadNotificationsForUser(Long userId) {
        return notificationRepository.countUnreadNotificationsForUser(userId);
    }

    /**
     * Count unread notifications for owner
     */
    public Long countUnreadNotificationsForOwner(Long ownerId) {
        return notificationRepository.countUnreadNotificationsForOwner(ownerId);
    }

    /**
     * Get active notifications for user
     */
    public List<NotificationDTO> getActiveNotificationsForUser(Long userId) {
        List<Notification> notifications = notificationRepository.getActiveNotificationsForUser(userId);
        return notifications.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Get notifications by type
     */
    public List<NotificationDTO> getNotificationsByType(Long userId, String notificationType) {
        List<Notification> notifications = notificationRepository.getNotificationsByType(userId, notificationType);
        return notifications.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Mark notification as read
     */
    public NotificationDTO markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());

        Notification updatedNotification = notificationRepository.save(notification);
        return convertToDTO(updatedNotification);
    }

    /**
     * Mark all unread notifications for user as read
     */
    public void markAllAsReadForUser(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.getUnreadNotificationsForUser(userId);
        unreadNotifications.forEach(notif -> {
            notif.setIsRead(true);
            notif.setReadAt(LocalDateTime.now());
        });
        notificationRepository.saveAll(unreadNotifications);
    }

    /**
     * Mark all unread notifications for owner as read
     */
    public void markAllAsReadForOwner(Long ownerId) {
        List<Notification> unreadNotifications = notificationRepository.getUnreadNotificationsForOwner(ownerId);
        unreadNotifications.forEach(notif -> {
            notif.setIsRead(true);
            notif.setReadAt(LocalDateTime.now());
        });
        notificationRepository.saveAll(unreadNotifications);
    }

    /**
     * Deactivate a notification
     */
    public void deactivateNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));
        notification.setIsActive(false);
        notificationRepository.save(notification);
    }

    /**
     * Delete a notification
     */
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    /**
     * Convert Notification entity to NotificationDTO
     */
    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUserId());
        dto.setOwnerId(notification.getOwnerId());
        dto.setNotificationContent(notification.getNotificationContent());
        dto.setNotificationType(notification.getNotificationType().toString());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setIsRead(notification.getIsRead());
        dto.setReadAt(notification.getReadAt());
        dto.setIsActive(notification.getIsActive());
        dto.setRelatedEntityType(notification.getRelatedEntityType());
        dto.setRelatedEntityId(notification.getRelatedEntityId());
        return dto;
    }
}
