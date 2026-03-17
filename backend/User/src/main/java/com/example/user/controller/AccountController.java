package com.example.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.user.modal.Account;
import com.example.user.service.AccountService;
import com.example.user.repository.UserRepository; // Added 

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/account")
@CrossOrigin(origins = "http://localhost:5173")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository; // CRITICAL: Added missing repository

    @PostMapping("/create")
    public ResponseEntity<?> createAccount(@RequestParam Long userId) {
        try {
            Account account = accountService.createAccount(userId);
            return new ResponseEntity<>(account, HttpStatus.CREATED);
        } catch (Exception e) {
            return buildErrorResponse("Failed to create account", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestParam Long userId,
                                   @RequestParam Double amount,
                                   @RequestParam(required = false) String transactionId) {
        try {
            Account account = accountService.deposit(userId, amount);
            return new ResponseEntity<>(account, HttpStatus.OK);
        } catch (RuntimeException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(
            @RequestParam("userId") Long userId,
            @RequestParam("amount") Double amount, 
            @RequestParam("transactionId") String transactionId) {
        try {
            Account account = accountService.withdraw(userId, amount);
            return ResponseEntity.ok(account);
        } catch (RuntimeException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Updated to match React handleTransfer: 
     * Uses fromUserId and toUserId (recipient.id)
     */
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(
            @RequestParam Long fromUserId,
            @RequestParam Long toUserId,
            @RequestParam Double amount,
            @RequestParam String transactionId) {
        try {
            accountService.performTransfer(fromUserId, toUserId, amount, transactionId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Transfer successful!");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // This catches "Insufficient balance", "User not found", etc.
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@RequestParam Long userId) {
        try {
            double balance = accountService.getBalance(userId);
            return new ResponseEntity<>(balance, HttpStatus.OK);
        } catch (RuntimeException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return buildErrorResponse("An error occurred while fetching balance", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * New endpoint: Return account details (balance + account number) in a single response.
     * This is used by the Owner Service when showing customer account info.
     */
    @GetMapping("/details")
    public ResponseEntity<?> getAccountDetails(@RequestParam Long userId) {
        try {
            var account = accountService.getAccountByUserId(userId);
            if (account == null) {
                return buildErrorResponse("Account not found", HttpStatus.NOT_FOUND);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("balance", account.getBalance());
            response.put("accountNumber", account.getAccountNumber());
            response.put("userId", userId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse("An error occurred while fetching account details", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Map<String, String>> buildErrorResponse(String msg, HttpStatus status) {
        Map<String, String> error = new HashMap<>();
        error.put("message", msg);
        return new ResponseEntity<>(error, status);
    }
}