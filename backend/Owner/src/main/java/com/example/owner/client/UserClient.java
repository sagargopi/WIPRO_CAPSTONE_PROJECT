package com.example.owner.client;

import com.example.owner.dto.UserDTO;
import com.example.owner.config.FeignConfig;
import com.example.owner.dto.AccountDTO;
import com.example.owner.dto.MessageDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "user-service", url = "http://localhost:8082", configuration = FeignConfig.class)
public interface UserClient {

    @GetMapping("/auth/users")
    List<UserDTO> getAllUsers();

    @PostMapping("/auth/register")
    UserDTO registerUser(@RequestBody Map<String, Object> userData);

    @PutMapping("/auth/users/{id}")
    UserDTO updateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates);

    @PostMapping("/loans/{loanId}/approve")
    String approveLoan(@PathVariable Long loanId);

    @PostMapping("/loans/{loanId}/reject")
    String rejectLoan(@PathVariable Long loanId);

    @PutMapping("/loans/{loanId}/details")
    void updateLoanDetails(@PathVariable Long loanId,
                           @RequestParam(required = false) Boolean visibility,
                           @RequestParam(required = false) Integer rating,
                           @RequestParam(required = false) String adminComments);

    /**
     * Get a user's balance from User microservice.
     * This call is used by the Owner service to display account info.
     * We forward the admin JWT token to keep this endpoint protected.
     */
    @GetMapping("/account/balance")
    Double getBalance(@RequestParam Long userId, @RequestHeader("Authorization") String authorization);

    /**
     * Returns full account details (balance + accountNumber) for a customer.
     */
    @GetMapping("/account/details")
    Map<String, Object> getAccountDetails(@RequestParam Long userId, @RequestHeader("Authorization") String authorization);

    /**
     * Get conversation between customer and admin from User microservice
     */
    @GetMapping("/api/messages/conversation")
    List<MessageDTO> getConversation(
            @RequestParam("userId") Long customerId,
            @RequestParam("ownerId") Long ownerId);
    /**
     * Get unread messages for user from User microservice
     */
    @GetMapping("/api/messages/unread/user/{userId}")
    List<MessageDTO> getUnreadMessagesForUser(@PathVariable Long userId);

    /**
     * Count unread messages for user from User microservice
     */
    @GetMapping("/api/messages/unread/count/user/{userId}")
    Long countUnreadMessagesForUser(@PathVariable Long userId);

    /**
     * Get all messages for a user from User microservice
     */
    @GetMapping("/api/messages/user/{userId}")
    List<MessageDTO> getMessagesForUser(@PathVariable Long userId);

    /**
     * Get all customer conversations for an admin from User microservice
     * Returns aggregated conversations with unread counts
     */
    @GetMapping("/api/messages/admin/{ownerId}/conversations")
    List<Map<String, Object>> getAdminConversations(@PathVariable Long ownerId);

    @DeleteMapping("/auth/users/{id}")
    void deleteUser(@RequestHeader("Authorization") String token,
                    @PathVariable Long id);

}


