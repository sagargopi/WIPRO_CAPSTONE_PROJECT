package com.example.owner.modal;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Message Entity for Owner/Admin Service
 * Represents chat messages between customers and admins
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long senderId;  // Customer userId or Admin ownerId

    @Column(nullable = false)
    private Long receiverId;  // Admin ownerId or Customer userId

    @Column(columnDefinition = "VARCHAR(1000)", nullable = false)
    private String messageText;

    @Column(nullable = false)
    private String messageType;  // TEXT, SYSTEM, etc.

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Constructors
    public Message(Long senderId, Long receiverId, String messageText, String messageType) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.messageType = messageType;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }
}
