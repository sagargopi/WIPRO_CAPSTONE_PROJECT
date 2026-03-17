package com.example.owner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.owner.modal.LoanApproval;
import java.util.List;

public interface LoanApprovalRepository extends JpaRepository<LoanApproval, Long> {
    
    List<LoanApproval> findByLoanId(Long loanId);
    
    List<LoanApproval> findByUserId(Long userId);
    
    List<LoanApproval> findByApprovalStatus(String approvalStatus);
    
    List<LoanApproval> findByUserIdAndApprovalStatus(Long userId, String approvalStatus);
    
    LoanApproval findByLoanIdAndUserId(Long loanId, Long userId);

    void deleteByUserId(Long userId);
}
