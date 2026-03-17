package com.example.owner.modal;

import jakarta.persistence.*;
import lombok.Data; // Import this
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;


@Entity
@Table(name = "notifications")
@Data // Automatically creates Getters, Setters, toString, etc.
@NoArgsConstructor // Creates empty constructor
@AllArgsConstructor // Creates constructor with all fields
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "notification_content", length = 500)
    private String notificationContent;

    @Column(name = "notification_type")
    private String notificationType;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    // Inside your Notification.java
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}