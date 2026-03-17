package com.example.user.dto;

import java.time.LocalDateTime;

/**
 * DTO for Message entity
 * Used for API responses and microservice communication
 */
public class MessageDTO {
    private Long id;
    private Long userId;
    private Long ownerId;
    private String messageContent;
    private String messageType;
    private LocalDateTime createdAt;
    private Boolean isRead;
    private LocalDateTime readAt;
    private String subject;

    // Constructors
    public MessageDTO() {}

    public MessageDTO(Long userId, Long ownerId, String messageContent, String messageType) {
        this.userId = userId;
        this.ownerId = ownerId;
        this.messageContent = messageContent;
        this.messageType = messageType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
