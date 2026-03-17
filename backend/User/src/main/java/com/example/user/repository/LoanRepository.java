package com.example.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.user.modal.Loan;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    
    List<Loan> findByUserId(Long userId);
    
    List<Loan> findByLoanStatus(String loanStatus);
    
    Loan findByIdAndUserId(Long id, Long userId);
    
    List<Loan> findByUserIdAndLoanStatus(Long userId, String loanStatus);

    List<Loan> findByLoanCategory(String loanCategory);

    List<Loan> findByVisibility(Boolean visibility);

    List<Loan> findByRatingGreaterThanEqual(Integer rating);

    List<Loan> findByLoanCategoryAndVisibility(String loanCategory, Boolean visibility);

    List<Loan> findByLoanCategoryContainingOrUserCommentsContaining(String categoryKeyword, String commentKeyword);

    void deleteByUserId(Long userId);
}
