package com.example.user.controller;

import com.example.user.dto.MessageDTO;
import com.example.user.modal.Message;
import com.example.user.repository.MessageRepository;
import com.example.user.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Chat/Message operations
 * Handles message creation, retrieval, and marking as read
 */
@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MessageController {

    @Autowired
    private MessageService messageService;
    
    @Autowired
    private MessageRepository messageRepository;

    /**
     * Send a reply message from admin to customer
     */
    @PostMapping("/reply")
    public ResponseEntity<MessageDTO> replyMessage(
            @RequestParam Long ownerId,
            @RequestParam Long userId,
            @RequestParam String messageContent,
            @RequestParam(required = false) String subject) {
        try {
            MessageDTO messageDTO = messageService.sendAdminMessage(userId, ownerId, messageContent, subject);
            return new ResponseEntity<>(messageDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

        /**
         * Send a message from customer to admin (matches frontend POST /send)
         */
        @PostMapping("/send")
        public ResponseEntity<MessageDTO> sendCustomerMessage(
                @RequestParam Long userId,
                @RequestParam Long ownerId,
                @RequestParam String messageContent,
                @RequestParam(required = false) String subject) {
            try {
                MessageDTO messageDTO = messageService.sendCustomerMessage(userId, ownerId, messageContent, subject);
                return new ResponseEntity<>(messageDTO, HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

    /**
     * Get all messages for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MessageDTO>> getMessagesForUser(@PathVariable Long userId) {
        try {
            List<MessageDTO> messages = messageService.getMessagesForUser(userId);
            return new ResponseEntity<>(messages, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all messages for an admin/owner
     */
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<MessageDTO>> getMessagesForOwner(@PathVariable Long ownerId) {
        try {
            List<MessageDTO> messages = messageService.getMessagesForOwner(ownerId);
            return new ResponseEntity<>(messages, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get conversation between user and owner
     */
    @GetMapping("/conversation")
    public ResponseEntity<List<MessageDTO>> getConversation(
            @RequestParam Long userId,
            @RequestParam Long ownerId) {
        try {
            List<MessageDTO> conversation = messageService.getConversation(userId, ownerId);
            return new ResponseEntity<>(conversation, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Inside User/src/main/java/com/example/user/controller/MessageController.java

    @PostMapping("/send-admin")
    public ResponseEntity<MessageDTO> sendAdminMessage(
            @RequestParam Long userId,
            @RequestParam Long ownerId,
            @RequestParam String messageContent,
            @RequestParam(required = false) String subject) {
        
        // This uses the service method we confirmed earlier
        MessageDTO savedMessage = messageService.sendAdminMessage(userId, ownerId, messageContent, subject);
        return ResponseEntity.ok(savedMessage);
    }

    /**
     * Get unread messages for user
     */
    @GetMapping("/unread/user/{userId}")
    public ResponseEntity<List<MessageDTO>> getUnreadMessagesForUser(@PathVariable Long userId) {
        try {
            List<MessageDTO> messages = messageService.getUnreadMessagesForUser(userId);
            return new ResponseEntity<>(messages, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get unread messages for owner
     */
    @GetMapping("/unread/owner/{ownerId}")
    public ResponseEntity<List<MessageDTO>> getUnreadMessagesForOwner(@PathVariable Long ownerId) {
        try {
            List<MessageDTO> messages = messageService.getUnreadMessagesForOwner(ownerId);
            return new ResponseEntity<>(messages, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Count unread messages for user
     */
    @GetMapping("/unread/count/user/{userId}")
    public ResponseEntity<Long> countUnreadMessagesForUser(@PathVariable Long userId) {
        try {
            Long count = messageService.countUnreadMessagesForUser(userId);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Count unread messages for owner
     */
    @GetMapping("/unread/count/owner/{ownerId}")
    public ResponseEntity<Long> countUnreadMessagesForOwner(@PathVariable Long ownerId) {
        try {
            Long count = messageService.countUnreadMessagesForOwner(ownerId);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Mark a message as read
     */
    @PutMapping("/{messageId}/read")
    public ResponseEntity<MessageDTO> markAsRead(@PathVariable Long messageId) {
        try {
            MessageDTO messageDTO = messageService.markAsRead(messageId);
            return new ResponseEntity<>(messageDTO, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Mark all unread messages for user as read
     */
    @PutMapping("/mark-all-read/user/{userId}")
    public ResponseEntity<String> markAllAsReadForUser(@PathVariable Long userId) {
        try {
            messageService.markAllAsReadForUser(userId);
            return new ResponseEntity<>("All messages marked as read", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Mark all unread messages for owner as read
     */
    @PutMapping("/mark-all-read/owner/{ownerId}")
    public ResponseEntity<String> markAllAsReadForOwner(@PathVariable Long ownerId) {
        try {
            messageService.markAllAsReadForOwner(ownerId);
            return new ResponseEntity<>("All messages marked as read", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete a message
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long messageId) {
        try {
            messageService.deleteMessage(messageId);
            return new ResponseEntity<>("Message deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all customer conversations with unread count for an admin
     */
    @GetMapping("/admin/{ownerId}/conversations")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> getAdminConversations(
            @PathVariable Long ownerId) {
        try {
            java.util.List<java.util.Map<String, Object>> conversations = messageService.getAdminConversations(ownerId);
            return new ResponseEntity<>(conversations, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Debug endpoint: Get all messages in database for debugging
     */
    @GetMapping("/debug/all-messages")
    public ResponseEntity<java.util.List<MessageDTO>> getAllMessages() {
        try {
            java.util.List<Message> allMessages = messageRepository.findAll();
            java.util.List<MessageDTO> dtos = allMessages.stream()
                    .map(msg -> {
                        MessageDTO dto = new MessageDTO();
                        dto.setId(msg.getId());
                        dto.setUserId(msg.getUserId());
                        dto.setOwnerId(msg.getOwnerId());
                        dto.setMessageContent(msg.getMessageContent());
                        dto.setMessageType(msg.getMessageType().toString());
                        dto.setCreatedAt(msg.getCreatedAt());
                        dto.setIsRead(msg.getIsRead());
                        dto.setReadAt(msg.getReadAt());
                        dto.setSubject(msg.getSubject());
                        return dto;
                    })
                    .collect(java.util.stream.Collectors.toList());
            System.out.println("📋 [DEBUG] Total messages in database: " + dtos.size());
            return new ResponseEntity<>(dtos, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Failed to get all messages: " + e.getMessage());
            return new ResponseEntity<>(new java.util.ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
