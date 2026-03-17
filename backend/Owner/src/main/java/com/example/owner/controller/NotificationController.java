package com.example.owner.controller;

import com.example.owner.service.NotificationService;
import com.example.owner.modal.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * FIX FOR 500 ERROR: Generic endpoint to receive notifications from Frontend
     * Used for: Download Tracking, Loan Status Updates, Announcements
     */
    @PostMapping("/send")
    public ResponseEntity<Notification> sendNotification(@RequestBody Map<String, Object> payload) {
        Notification notification = new Notification();
        
        // 1. Map "message" from React to "notificationContent" in DB
        notification.setNotificationContent((String) payload.get("message"));
        
        // 2. Map "type" from React to "notificationType" in DB
        notification.setNotificationType((String) payload.get("type"));

        // 3. Logic to determine if recipient is an Owner (Admin) or a User
        // In your system, Admin/Owner is usually ID 1
        Long recipientId = Long.valueOf(payload.get("recipientId").toString());
        
        if (recipientId == 1) {
            notification.setOwnerId(1L); // Notifies the Admin
        } else {
            notification.setUserId(recipientId); // Notifies a specific Customer
        }

        Notification saved = notificationService.saveNotification(notification);
        return ResponseEntity.ok(saved);
    }

    /**
     * Requirement: Admin receives notifications when customer balance reaches zero
     */
    @PostMapping("/balance-alert")
    public ResponseEntity<Void> receiveBalanceAlert(
            @RequestParam Long userId, 
            @RequestParam String message) {
        
        // Save alert to owner_db with ownerId = 1 (System Admin)
        notificationService.createBalanceAlert(userId, 1L, message);
        return ResponseEntity.ok().build();
    }

    /**
     * Requirement: Owner announcement of updates
     */
    @PostMapping("/announcement")
    public ResponseEntity<Void> createAnnouncement(
            @RequestParam Long ownerId, 
            @RequestParam String message) {
        notificationService.createAnnouncement(ownerId, message);
        return ResponseEntity.ok().build();
    }

    /**
     * Fetch notifications for the Admin Dashboard
     */
    @GetMapping("/owner/{ownerId}")
    public List<Notification> getOwnerNotifications(@PathVariable Long ownerId) {
        return notificationService.getNotificationsForOwner(ownerId);
    }

    /**
     * Fetch notifications for the Customer Dashboard
     */
    @GetMapping("/user/{userId}")
    public List<Notification> getUserNotifications(@PathVariable Long userId) {
        return notificationService.getNotificationsForUser(userId);
    }
}