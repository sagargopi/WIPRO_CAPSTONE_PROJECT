package com.example.owner.service;

import com.example.owner.modal.Notification;
import com.example.owner.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

   public Notification saveNotification(Notification notification) {
        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(LocalDateTime.now());
        }
        return notificationRepository.save(notification);
    }

    public void createBalanceAlert(Long userId, Long ownerId, String message) {
        Notification alert = new Notification();
        alert.setUserId(userId);
        alert.setOwnerId(ownerId);
        alert.setNotificationContent(message);
        alert.setNotificationType("BALANCE_ZERO");
        alert.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(alert);
    }

    /**
     * Requirement: Notification to Owner when a loan application is submitted
     */
    public void createLoanStatusNotification(Long userId, Long ownerId, String message, Long loanId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setOwnerId(ownerId);
        notification.setNotificationContent(message);
        notification.setNotificationType("LOAN_STATUS");
        notification.setRelatedEntityId(loanId);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);
        notification.setIsActive(true);
        notificationRepository.save(notification);
    }

    /**
     * Requirement: Owner announcement of updates
     */
    public void createAnnouncement(Long ownerId, String message) {
        Notification announcement = new Notification();
        announcement.setOwnerId(ownerId);
        announcement.setNotificationContent(message);
        announcement.setNotificationType("ANNOUNCEMENT");
        announcement.setCreatedAt(LocalDateTime.now());
        announcement.setIsRead(false);
        announcement.setIsActive(true);
        // Special case: null userId means global announcement
        announcement.setUserId(null); 
        notificationRepository.save(announcement);
    }

    public List<Notification> getNotificationsForOwner(Long ownerId) {
        return notificationRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}