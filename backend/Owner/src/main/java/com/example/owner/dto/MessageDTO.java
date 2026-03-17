package com.example.owner.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO for Message communication between Owner microservice and clients
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    
    // Accept either 'senderId' (local) or 'userId' (from user-service)
    @JsonAlias({"userId", "senderId"})
    private Long senderId;
    
    // Accept either 'receiverId' (local) or 'ownerId' (from user-service)
    @JsonAlias({"ownerId", "receiverId"})
    private Long receiverId;
    
    // Accept either 'messageText' (local) or 'messageContent' (from user-service)
    @JsonAlias({"messageContent", "messageText"})
    private String messageText;
    
    private String messageType;
    private Boolean isRead;
    private LocalDateTime createdAt;
}