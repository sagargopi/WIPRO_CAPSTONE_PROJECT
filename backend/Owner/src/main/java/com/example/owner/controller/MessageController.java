package com.example.owner.controller;

import com.example.owner.dto.MessageDTO;
import com.example.owner.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Admin Chat/Message operations
 * Allows admins to view customer messages and send replies
 */
@RestController
@RequestMapping("/api/admin/messages")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * Get all customer conversations (aggregated by customer with unread count)
     */
    @GetMapping("/conversations/admin/{ownerId}")
    public ResponseEntity<List<Map<String, Object>>> getCustomerConversations(
            @PathVariable Long ownerId) {
        try {
            List<Map<String, Object>> conversations = messageService.getCustomerConversations(ownerId);
            return new ResponseEntity<>(conversations, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Send reply from admin to customer
     */
    @PostMapping("/reply")
    public ResponseEntity<MessageDTO> sendAdminReply(
            @RequestParam Long ownerId,
            @RequestParam Long userId,
            @RequestParam String messageText) {
        try {
            MessageDTO messageDTO = messageService.sendAdminReply(ownerId, userId, messageText);
            return new ResponseEntity<>(messageDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get conversation between customer and admin
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

    /**
     * Get all unread messages for admin
     */
    @GetMapping("/unread/admin/{ownerId}")
    public ResponseEntity<List<MessageDTO>> getUnreadMessagesForAdmin(@PathVariable Long ownerId) {
        try {
            List<MessageDTO> messages = messageService.getUnreadMessagesForAdmin(ownerId);
            return new ResponseEntity<>(messages, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Count unread messages for admin
     */
    @GetMapping("/unread/count/admin/{ownerId}")
    public ResponseEntity<Long> countUnreadMessagesForAdmin(@PathVariable Long ownerId) {
        try {
            Long count = messageService.countUnreadMessagesForAdmin(ownerId);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all messages received by admin
     */
    @GetMapping("/admin/{ownerId}")
    public ResponseEntity<List<MessageDTO>> getMessagesForAdmin(@PathVariable Long ownerId) {
        try {
            List<MessageDTO> messages = messageService.getMessagesForAdmin(ownerId);
            return new ResponseEntity<>(messages, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
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
     * Delete a message
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long messageId) {
        try {
            messageService.deleteMessage(messageId);
            return new ResponseEntity<>("Message deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting message", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
