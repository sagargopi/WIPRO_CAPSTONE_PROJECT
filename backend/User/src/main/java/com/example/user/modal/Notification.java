package com.example.user.modal;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data                       // Generates Getters, Setters, toString, equals, and hashCode
@Builder                    // Allows you to use Notification.builder()...
@NoArgsConstructor          // Required by JPA
@AllArgsConstructor         // Required by @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long ownerId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String notificationContent;

    @Enumerated(EnumType.STRING) // <--- THIS IS THE CRITICAL LINE
    @Column(name = "notification_type", length = 50, nullable = false)
    private NotificationType notificationType; // DOWNLOAD_TRACKING, BALANCE_ALERT, LOAN_STATUS, etc.

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private Boolean isRead = false;

    private LocalDateTime readAt;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    private String relatedEntityType; // TRANSACTION, LOAN, ACCOUNT
    private Long relatedEntityId;
}