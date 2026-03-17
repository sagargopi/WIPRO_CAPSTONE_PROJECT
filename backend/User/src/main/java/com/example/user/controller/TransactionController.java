package com.example.user.controller;

import com.example.user.modal.Transaction;
import com.example.user.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/create")
    public Transaction createTransaction(@RequestParam String accountNumber,
                                         @RequestParam String type,
                                         @RequestParam double amount,
                                         @RequestParam String status){

        return transactionService.createTransaction(accountNumber, type, amount, status);
    }

    @GetMapping("/history")
    public List<Transaction> getTransactions(@RequestParam String accountNumber){

        return transactionService.getTransactions(accountNumber);

    }
}