package com.example.user.service;

import com.example.user.modal.Transaction;
import com.example.user.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public Transaction createTransaction(String accountNumber, String type, double amount, String status){

        Transaction transaction = new Transaction();

        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setAccountNumber(accountNumber);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setStatus(status);
        transaction.setDate(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactions(String accountNumber){

        return transactionRepository.findByAccountNumber(accountNumber);

    }
}