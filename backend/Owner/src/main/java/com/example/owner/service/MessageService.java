package com.example.owner.service;

import com.example.owner.client.UserClient;
import com.example.owner.dto.MessageDTO;
import com.example.owner.modal.Message;
import com.example.owner.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for handling admin chat messages
 * Calls User microservice to fetch customer messages
 * Allows admins to view and respond to customer messages
 */
@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserClient userClient;

    /**
     * Send a reply message from admin to customer
     */
    public MessageDTO sendAdminReply(Long adminId, Long customerId, String messageText) {
        Message message = new Message(adminId, customerId, messageText, "TEXT");
        Message savedMessage = messageRepository.save(message);
        return convertToDTO(savedMessage);
    }

    /**
     * Get conversation between customer and admin
     * Fetches messages from User microservice and combines with admin replies
     */
    public List<MessageDTO> getConversation(Long customerId, Long adminId) {
        try {
            System.out.println("🔄 [Owner] Fetching conversation: customerId=" + customerId + ", adminId=" + adminId);
            // Fetch conversation from User microservice
            List<MessageDTO> userMessages = userClient.getConversation(customerId, adminId);
            System.out.println("✅ [Owner] Received " + (userMessages != null ? userMessages.size() : 0) + " messages from User service for customerId: " + customerId);
            
            // Add admin replies from Owner database
            List<Message> adminReplies = messageRepository.getConversation(customerId, adminId);
            System.out.println("💬 [Owner] Found " + adminReplies.size() + " admin replies in local database");
            
            List<MessageDTO> adminReplyDTOs = adminReplies.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            // Combine both lists and sort by timestamp
            if (userMessages == null) userMessages = new java.util.ArrayList<>();
            userMessages.addAll(adminReplyDTOs);
            userMessages.sort((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()));
            
            System.out.println("✅ [Owner] Total messages in conversation: " + userMessages.size());
            return userMessages;
        } catch (Exception e) {
            System.err.println("❌ [Owner] Error fetching conversation from User microservice: " + e.getMessage());
            e.printStackTrace();
            // Fallback to local messages only
            List<Message> messages = messageRepository.getConversation(customerId, adminId);
            System.out.println("⚠️  [Owner] Fallback: Returning " + messages.size() + " local messages only");
            return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
        }
    }

    /**
     * Get all unread customer messages (from User microservice)
     * This creates a list of unique customers with unread messages
     */
    public List<MessageDTO> getUnreadMessagesForAdmin(Long adminId) {
        try {
            // Fetch unread messages from User microservice for multiple customer IDs
            // For this, we'd need a bulk endpoint - for now, fetch sample customers
            List<MessageDTO> unreadMessages = new java.util.ArrayList<>();
            
            // Get messages from User microservice - we need to call this for each customer
            // For MVP, return local unread messages and indicate there are customer messages
            List<Message> localMessages = messageRepository.getUnreadMessagesForAdmin(adminId);
            return localMessages.stream().map(this::convertToDTO).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching unread messages: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Get unique customer conversations with their latest message
     * Fetches from User microservice
     */
    public List<Map<String, Object>> getCustomerConversations(Long adminId) {
        try {
            System.out.println("🔄 [Owner Service] Calling UserClient.getAdminConversations for adminId: " + adminId);
            // Fetch all conversations from User microservice
            List<Map<String, Object>> conversations = userClient.getAdminConversations(adminId);
            System.out.println("✅ [Owner Service] Received " + (conversations != null ? conversations.size() : 0) + " conversations from User service");
            if (conversations != null && !conversations.isEmpty()) {
                conversations.forEach(conv -> System.out.println("   - Customer: " + conv));
            }
            return conversations;
        } catch (Exception e) {
            System.err.println("❌ [Owner Service] Error fetching customer conversations from User microservice: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Get all messages received by admin
     */
    public List<MessageDTO> getMessagesForAdmin(Long adminId) {
        try {
            List<Message> messages = messageRepository.findByReceiverId(adminId);
            return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching messages: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Count unread messages for admin
     * Gets count from local database (admin conversation messages)
     */
    public Long countUnreadMessagesForAdmin(Long adminId) {
        try {
            // Get unread messages from local database
            List<Message> unreadMessages = messageRepository.getUnreadMessagesForAdmin(adminId);
            return (long) unreadMessages.size();
        } catch (Exception e) {
            System.err.println("Error counting unread messages: " + e.getMessage());
            return 0L;
        }
    }

    /**
     * Mark a message as read
     */
    public MessageDTO markAsRead(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setIsRead(true);
        Message updatedMessage = messageRepository.save(message);
        return convertToDTO(updatedMessage);
    }

    /**
     * Mark all messages in a conversation as read
     */
    public void markConversationAsRead(Long customerId, Long adminId) {
        List<Message> messages = messageRepository.getConversation(customerId, adminId);
        for (Message message : messages) {
            if (!message.getIsRead() && message.getReceiverId().equals(adminId)) {
                message.setIsRead(true);
                messageRepository.save(message);
            }
        }
    }

    /**
     * Delete a message
     */
    public void deleteMessage(Long messageId) {
        messageRepository.deleteById(messageId);
    }

    /**
     * Convert Message entity to DTO
     */
    private MessageDTO convertToDTO(Message message) {
        return new MessageDTO(
                message.getId(),
                message.getSenderId(),
                message.getReceiverId(),
                message.getMessageText(),
                message.getMessageType(),
                message.getIsRead(),
                message.getCreatedAt()
        );
    }
}
