package com.example.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.user.client.OwnerClient;
import com.example.user.modal.Account;
import com.example.user.repository.AccountRepository;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private OwnerClient ownerClient; 

    public Account createAccount(Long userId) {
        Account account = new Account();
        account.setUserId(userId);
        account.setBalance(0.0);
        return accountRepository.save(account);
    }

    public Account deposit(Long userId, double amount) {
        Account account = accountRepository.findByUserId(userId);
        if (account == null) throw new RuntimeException("Account not found");
        
        account.setBalance(account.getBalance() + amount);
        Account updated = accountRepository.save(account);
        transactionService.createTransaction(updated.getAccountNumber(), "DEPOSIT", amount, "SUCCESS");
        return updated;
    }

    public Account withdraw(Long userId, double amount) {
        Account account = accountRepository.findByUserId(userId);
        if (account == null) throw new RuntimeException("Account not found");

        if (amount > (account.getBalance() + 0.01)) { 
            throw new RuntimeException("Insufficient balance");
        }

        double newBalance = account.getBalance() - amount;
        account.setBalance(newBalance < 0.01 ? 0.0 : newBalance);
        Account updated = accountRepository.save(account);

        transactionService.createTransaction(updated.getAccountNumber(), "WITHDRAW", amount, "SUCCESS");
        checkAndNotifyAdmin(userId, updated.getBalance());
        return updated;
    }

    @Transactional
    public void performTransfer(Long fromUserId, Long toUserId, Double amount, String transactionId) {
        Account fromAccount = accountRepository.findByUserId(fromUserId);
        Account toAccount = accountRepository.findByUserId(toUserId);

        if (fromAccount == null || toAccount == null) {
            throw new RuntimeException("Account not found");
        }

        if (fromAccount.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance for transfer");
        }

        // 1. Update Balances
        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);

        // 2. Save
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // 3. Create History (Audit Trail)
        transactionService.createTransaction(fromAccount.getAccountNumber(), "TRANSFER_DEBIT", amount, "SUCCESS");
        transactionService.createTransaction(toAccount.getAccountNumber(), "TRANSFER_CREDIT", amount, "SUCCESS");
        
        checkAndNotifyAdmin(fromUserId, fromAccount.getBalance());
        
        System.out.println("Transfer successful: " + transactionId);
    }

    // This method was "undefined" in your error log because of the syntax errors above it
    private void checkAndNotifyAdmin(Long userId, double currentBalance) {
        if (currentBalance <= 0) {
            try {
                String message = "CRITICAL ALERT: Customer ID " + userId + " has reached a zero balance.";
                ownerClient.sendAdminNotification(message, userId);
            } catch (Exception e) {
                System.err.println("❌ Notification failed: " + e.getMessage());
            }
        }
    }

    public Account getAccountByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public double getBalance(Long userId) {
        Account account = accountRepository.findByUserId(userId);
        if (account == null) throw new RuntimeException("Account not found");
        return account.getBalance();
    }
} // End of Class