package com.example.user.controller;

import java.util.Map;
import java.time.LocalDateTime;
import java.util.List;
import com.example.user.modal.Notification;
import com.example.user.dto.NotificationDTO;
import com.example.user.modal.NotificationType;
import com.example.user.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Notification operations
 * Handles notification creation, retrieval, and marking as read
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<?> handleGenericNotification(@RequestBody Map<String, Object> payload) {
        try {
            // Create a new notification object
            Notification notification = new Notification();
            
            // 1. Map content
            notification.setNotificationContent(payload.get("message").toString());
            
            // 2. Map Enum (Important: must match your NotificationType Enum)
            String typeStr = payload.get("type").toString();
            notification.setNotificationType(NotificationType.valueOf(typeStr));

            // 3. Handle IDs (recipientId 1 = Admin)
            Long rId = Long.parseLong(payload.get("recipientId").toString());
            if (rId == 1L) {
                notification.setOwnerId(1L);
                notification.setUserId(0L); // Default value to prevent SQL errors
            } else {
                notification.setUserId(rId);
                notification.setOwnerId(1L);
            }

            // 4. Set required defaults
            notification.setIsRead(false);
            notification.setIsActive(true);
            notification.setCreatedAt(LocalDateTime.now());

            // Save via service
            // Note: If your service expects a DTO, you may need to convert here
            notificationService.saveNotification(notification); 

            return ResponseEntity.status(HttpStatus.CREATED).body("Notification Logged");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Backend Error: " + e.getMessage());
        }
    }

    /**
     * Create a balance alert notification
     */
    @PostMapping("/balance-alert")
    public ResponseEntity<NotificationDTO> createBalanceAlert(
            @RequestParam Long userId,
            @RequestParam Long ownerId,
            @RequestParam String message) {
        try {
            NotificationDTO notificationDTO = notificationService.createBalanceAlert(userId, ownerId, message);
            return new ResponseEntity<>(notificationDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Create a loan status notification
     */
    @PostMapping("/loan-status")
    public ResponseEntity<NotificationDTO> createLoanStatusNotification(
            @RequestParam Long userId,
            @RequestParam Long ownerId,
            @RequestParam String message,
            @RequestParam Long loanId) {
        try {
            NotificationDTO notificationDTO = notificationService.createLoanStatusNotification(userId, ownerId, message, loanId);
            return new ResponseEntity<>(notificationDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint for Feign Client from Owner Service to trigger loan notifications
     */
    @PostMapping("/loan-update")
    public ResponseEntity<Void> sendLoanNotification(
            @RequestParam Long userId, 
            @RequestParam String message,
            @RequestParam Long loanId) {
        try {
            // We use the system owner ID (1) or a default for these automated alerts
            notificationService.createLoanStatusNotification(userId, 1L, message, loanId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Create a transaction notification
     */
    @PostMapping("/transaction")
    public ResponseEntity<NotificationDTO> createTransactionNotification(
            @RequestParam Long userId,
            @RequestParam Long ownerId,
            @RequestParam String message,
            @RequestParam Long transactionId) {
        try {
            NotificationDTO notificationDTO = notificationService.createTransactionNotification(userId, ownerId, message, transactionId);
            return new ResponseEntity<>(notificationDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Create a system notification
     */
    @PostMapping("/system")
    public ResponseEntity<NotificationDTO> createSystemNotification(
            @RequestParam Long userId,
            @RequestParam Long ownerId,
            @RequestParam String message) {
        try {
            NotificationDTO notificationDTO = notificationService.createSystemNotification(userId, ownerId, message);
            return new ResponseEntity<>(notificationDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all notifications for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsForUser(@PathVariable Long userId) {
        try {
            List<NotificationDTO> notifications = notificationService.getNotificationsForUser(userId);
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all notifications for an owner/admin
     */
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsForOwner(@PathVariable Long ownerId) {
        try {
            List<NotificationDTO> notifications = notificationService.getNotificationsForOwner(ownerId);
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get unread notifications for user
     */
    @GetMapping("/unread/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotificationsForUser(@PathVariable Long userId) {
        try {
            List<NotificationDTO> notifications = notificationService.getUnreadNotificationsForUser(userId);
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get unread notifications for owner
     */
    @GetMapping("/unread/owner/{ownerId}")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotificationsForOwner(@PathVariable Long ownerId) {
        try {
            List<NotificationDTO> notifications = notificationService.getUnreadNotificationsForOwner(ownerId);
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Count unread notifications for user
     */
    @GetMapping("/unread/count/user/{userId}")
    public ResponseEntity<Long> countUnreadNotificationsForUser(@PathVariable Long userId) {
        try {
            Long count = notificationService.countUnreadNotificationsForUser(userId);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Count unread notifications for owner
     */
    @GetMapping("/unread/count/owner/{ownerId}")
    public ResponseEntity<Long> countUnreadNotificationsForOwner(@PathVariable Long ownerId) {
        try {
            Long count = notificationService.countUnreadNotificationsForOwner(ownerId);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get active notifications for user
     */
    @GetMapping("/active/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> getActiveNotificationsForUser(@PathVariable Long userId) {
        try {
            List<NotificationDTO> notifications = notificationService.getActiveNotificationsForUser(userId);
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get notifications by type
     */
    @GetMapping("/type/{userId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByType(
            @PathVariable Long userId,
            @RequestParam String notificationType) {
        try {
            List<NotificationDTO> notifications = notificationService.getNotificationsByType(userId, notificationType);
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Mark a notification as read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable Long notificationId) {
        try {
            NotificationDTO notificationDTO = notificationService.markAsRead(notificationId);
            return new ResponseEntity<>(notificationDTO, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Mark all unread notifications for user as read
     */
    @PutMapping("/mark-all-read/user/{userId}")
    public ResponseEntity<String> markAllAsReadForUser(@PathVariable Long userId) {
        try {
            notificationService.markAllAsReadForUser(userId);
            return new ResponseEntity<>("All notifications marked as read", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Mark all unread notifications for owner as read
     */
    @PutMapping("/mark-all-read/owner/{ownerId}")
    public ResponseEntity<String> markAllAsReadForOwner(@PathVariable Long ownerId) {
        try {
            notificationService.markAllAsReadForOwner(ownerId);
            return new ResponseEntity<>("All notifications marked as read", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deactivate a notification
     */
    @PutMapping("/{notificationId}/deactivate")
    public ResponseEntity<String> deactivateNotification(@PathVariable Long notificationId) {
        try {
            notificationService.deactivateNotification(notificationId);
            return new ResponseEntity<>("Notification deactivated", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete a notification
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<String> deleteNotification(@PathVariable Long notificationId) {
        try {
            notificationService.deleteNotification(notificationId);
            return new ResponseEntity<>("Notification deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
