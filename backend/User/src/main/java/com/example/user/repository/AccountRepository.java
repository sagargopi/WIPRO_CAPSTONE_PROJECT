package com.example.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.user.modal.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
    
    Account findByAccountNumber(String accountNumber);
    
    Account findByUserId(Long userId);

    void deleteByUserId(Long userId);

}