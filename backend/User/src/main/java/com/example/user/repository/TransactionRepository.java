package com.example.user.repository;

import com.example.user.modal.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountNumber(String accountNumber);

    void deleteByAccountNumber(String accountNumber);
}