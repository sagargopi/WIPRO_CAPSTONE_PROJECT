package com.example.user.service;

import com.example.user.dto.MessageDTO;
import com.example.user.repository.UserRepository;
import com.example.user.modal.User;
import com.example.user.modal.Message;
import com.example.user.modal.MessageType;
import com.example.user.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling chat messages between customers and admins
 * Includes message creation, retrieval, and marking as read
 */
@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Send a message from customer to admin
     */
    public MessageDTO sendCustomerMessage(Long userId, Long ownerId, String messageContent, String subject) {
        Message message = new Message();
        message.setUserId(userId);
        message.setOwnerId(ownerId);
        message.setMessageContent(messageContent);
        message.setMessageType(MessageType.CUSTOMER_TO_ADMIN);
        message.setSubject(subject);
        message.setCreatedAt(LocalDateTime.now());
        message.setIsRead(false);

        Message savedMessage = messageRepository.save(message);
        return convertToDTO(savedMessage);
    }

    /**
     * Send a reply message from admin to customer
     */
    public MessageDTO sendAdminMessage(Long userId, Long ownerId, String messageContent, String subject) {
        Message message = new Message();
        message.setUserId(userId);
        message.setOwnerId(ownerId);
        message.setMessageContent(messageContent);
        message.setMessageType(MessageType.ADMIN_TO_CUSTOMER);
        message.setSubject(subject);
        message.setCreatedAt(LocalDateTime.now());
        message.setIsRead(false);

        Message savedMessage = messageRepository.save(message);
        return convertToDTO(savedMessage);
    }

    /**
     * Get all messages for a user
     */
    public List<MessageDTO> getMessagesForUser(Long userId) {
        List<Message> messages = messageRepository.findByUserId(userId);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Get all messages for an admin/owner
     */
    public List<MessageDTO> getMessagesForOwner(Long ownerId) {
        List<Message> messages = messageRepository.findByOwnerId(ownerId);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Get conversation between user and owner
     */
    public List<MessageDTO> getConversation(Long userId, Long ownerId) {
        List<Message> messages = messageRepository.getConversation(userId, ownerId);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Get unread messages for user
     */
    public List<MessageDTO> getUnreadMessagesForUser(Long userId) {
        List<Message> messages = messageRepository.getUnreadMessagesForUser(userId);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Get unread messages for owner
     */
    public List<MessageDTO> getUnreadMessagesForOwner(Long ownerId) {
        List<Message> messages = messageRepository.getUnreadMessagesForOwner(ownerId);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Count unread messages for user
     */
    public Long countUnreadMessagesForUser(Long userId) {
        return messageRepository.countUnreadMessagesForUser(userId);
    }

    /**
     * Count unread messages for owner
     */
    public Long countUnreadMessagesForOwner(Long ownerId) {
        return messageRepository.countUnreadMessagesForOwner(ownerId);
    }

    /**
     * Mark message as read
     */
    public MessageDTO markAsRead(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found with id: " + messageId));

        message.setIsRead(true);
        message.setReadAt(LocalDateTime.now());

        Message updatedMessage = messageRepository.save(message);
        return convertToDTO(updatedMessage);
    }

    /**
     * Mark all unread messages for user as read
     */
    public void markAllAsReadForUser(Long userId) {
        List<Message> unreadMessages = messageRepository.getUnreadMessagesForUser(userId);
        unreadMessages.forEach(msg -> {
            msg.setIsRead(true);
            msg.setReadAt(LocalDateTime.now());
        });
        messageRepository.saveAll(unreadMessages);
    }

    /**
     * Mark all unread messages for owner as read
     */
    public void markAllAsReadForOwner(Long ownerId) {
        List<Message> unreadMessages = messageRepository.getUnreadMessagesForOwner(ownerId);
        unreadMessages.forEach(msg -> {
            msg.setIsRead(true);
            msg.setReadAt(LocalDateTime.now());
        });
        messageRepository.saveAll(unreadMessages);
    }

    /**
     * Delete a message
     */
    public void deleteMessage(Long messageId) {
        messageRepository.deleteById(messageId);
    }

    /**
     * Get all administrator conversations grouped by customer ID
     * Returns list of conversations with unread count and latest message for each customer
     */
    public java.util.List<java.util.Map<String, Object>> getAdminConversations(Long ownerId) {
        try {
            // Get all messages for this admin
            List<Message> allMessages = messageRepository.findByOwnerId(ownerId);
            System.out.println("📊 [DEBUG] Found " + allMessages.size() + " messages for ownerId: " + ownerId);
            
            // Group by userId (customer)
            java.util.Map<Long, java.util.List<Message>> groupedByCustomer = allMessages.stream()
                    .collect(Collectors.groupingBy(Message::getUserId));
            
            System.out.println("👥 [DEBUG] Found " + groupedByCustomer.size() + " unique customers");
            
            // Convert to conversation format with unread count
            java.util.List<java.util.Map<String, Object>> conversations = new java.util.ArrayList<>();
            
            for (java.util.Map.Entry<Long, java.util.List<Message>> entry : groupedByCustomer.entrySet()) {
                java.util.List<Message> customerMessages = entry.getValue();
                
                // Count unread messages from customer to admin
                long unreadCount = customerMessages.stream()
                        .filter(m -> !m.getIsRead() && m.getMessageType() == MessageType.CUSTOMER_TO_ADMIN)
                        .count();
                
                // Get latest message
                Message latestMsg = customerMessages.stream()
                        .max(java.util.Comparator.comparing(Message::getCreatedAt))
                        .orElse(null);
                
                if (latestMsg != null) {
                    java.util.Map<String, Object> conv = new java.util.HashMap<>();
                    conv.put("customerId", entry.getKey());
                    
                    String username = userRepository.findById(entry.getKey())
                            .map(User::getUsername)
                            .orElse("Customer #" + entry.getKey());
                    conv.put("customerName", username);
                    
                    conv.put("unreadCount", unreadCount);
                    conv.put("lastMessage", latestMsg.getMessageContent());
                    conv.put("lastMessageTime", latestMsg.getCreatedAt());
                    conv.put("subject", latestMsg.getSubject());
                    conversations.add(conv);
                    
                    System.out.println("💬 [DEBUG] Customer " + entry.getKey() + " (" + username + "): " + unreadCount + " unread messages");
                }
            }
            
            // Sort by latest message time, descending
            conversations.sort((c1, c2) -> {
                java.time.LocalDateTime t1 = (java.time.LocalDateTime) c1.get("lastMessageTime");
                java.time.LocalDateTime t2 = (java.time.LocalDateTime) c2.get("lastMessageTime");
                return t2.compareTo(t1);
            });
            
            System.out.println("✅ [DEBUG] Returning " + conversations.size() + " conversations");
            return conversations;
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Exception in getAdminConversations: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Convert Message entity to MessageDTO
     */
    private MessageDTO convertToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setUserId(message.getUserId());
        dto.setOwnerId(message.getOwnerId());
        dto.setMessageContent(message.getMessageContent());
        dto.setMessageType(message.getMessageType().toString());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setIsRead(message.getIsRead());
        dto.setReadAt(message.getReadAt());
        dto.setSubject(message.getSubject());
        return dto;
    }
}
