package com.example.owner.service;

import com.example.owner.client.UserClient;
import com.example.owner.modal.LoanApproval;
import com.example.owner.client.NotificationClient;
import com.example.owner.repository.LoanApprovalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class LoanApprovalService {

    @Autowired
    private LoanApprovalRepository loanApprovalRepository;

    @Autowired
    private UserClient userClient;

// Inside Owner/src/main/java/com/example/owner/service/LoanApprovalService.java

    @Autowired
    private NotificationClient notificationClient;

    /**
     * Create a loan approval record from User service
     */
    public LoanApproval createLoanApprovalRecord(Long loanId, Long userId, Double loanAmount, 
                                                  Double interestRate, Integer tenureMonths, Double monthlyEmi, String category, String userComments) {
        LoanApproval approval = new LoanApproval(loanId, userId, loanAmount, interestRate, tenureMonths);
        approval.setMonthlyEmi(monthlyEmi);
        approval.setLoanCategory(category);
        approval.setUserComments(userComments);
        approval.setApprovalStatus("PENDING");
        
        return loanApprovalRepository.save(approval);
    }

    /**
     * Get all pending loan approvals
     */
    public List<LoanApproval> getPendingApprovals() {
        return loanApprovalRepository.findByApprovalStatus("PENDING");
    }

    /**
     * Get pending approvals for a specific user
     */
    public List<LoanApproval> getPendingApprovalsForUser(Long userId) {
        return loanApprovalRepository.findByUserIdAndApprovalStatus(userId, "PENDING");
    }

    /**
     * Get all approvals for a user
     */
    public List<LoanApproval> getUserApprovals(Long userId) {
        return loanApprovalRepository.findByUserId(userId);
    }

    /**
     * Get a specific approval
     */
    public LoanApproval getApprovalById(Long approvalId) {
        return loanApprovalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval not found"));
    }

    /**
     * Get approvals by status
     */
    public List<LoanApproval> getApprovalsByStatus(String status) {
        return loanApprovalRepository.findByApprovalStatus(status);
    }

    /**
     * Approve a loan
     */
    public LoanApproval approveLoan(Long approvalId, Long adminId, String comments) {
        LoanApproval approval = getApprovalById(approvalId);
        
        if (!"PENDING".equals(approval.getApprovalStatus())) {
            throw new RuntimeException("Loan approval is not in PENDING status");
        }

        approval.setApprovalStatus("APPROVED");
        approval.setReviewedBy(adminId);
        approval.setReviewDate(LocalDate.now());
        approval.setComments(comments);

        // 1. Call User service to approve the loan record
        userClient.approveLoan(approval.getLoanId());

        // 2. TRIGGER NOTIFICATION: Tell the User Service to notify the customer
        try {
            String message = "Congratulations! Your loan application #" + approval.getLoanId() + " has been approved.";
            notificationClient.sendLoanNotification(approval.getUserId(), message, approval.getLoanId());
            System.out.println("✅ Notification sent to User #" + approval.getUserId() + " for Approval");
        } catch (Exception e) {
            System.err.println("❌ Failed to send notification via Feign: " + e.getMessage());
        }

        return loanApprovalRepository.save(approval);
    }

    /**
     * Reject a loan
     */
    public LoanApproval rejectLoan(Long approvalId, Long adminId, String comments) {
        LoanApproval approval = getApprovalById(approvalId);
        
        if (!"PENDING".equals(approval.getApprovalStatus())) {
            throw new RuntimeException("Loan approval is not in PENDING status");
        }

        approval.setApprovalStatus("REJECTED");
        approval.setReviewedBy(adminId);
        approval.setReviewDate(LocalDate.now());
        approval.setComments(comments);

        // 1. Call User service to reject the loan record
        userClient.rejectLoan(approval.getLoanId());

        // 2. TRIGGER NOTIFICATION: Tell the User Service to notify the customer
        try {
            String message = "We regret to inform you that your loan application #" + approval.getLoanId() + " was rejected.";
            notificationClient.sendLoanNotification(approval.getUserId(), message, approval.getLoanId());
            System.out.println("✅ Notification sent to User #" + approval.getUserId() + " for Rejection");
        } catch (Exception e) {
            System.err.println("❌ Failed to send notification via Feign: " + e.getMessage());
        }

        return loanApprovalRepository.save(approval);
    }

    // Add to Owner/src/main/java/com/example/owner/service/LoanApprovalService.java

    public void deleteLoanApproval(Long approvalId) {
        LoanApproval approval = getApprovalById(approvalId);
        
        // Optional: Inform User Service to clear/reject the loan on their end
        try {
            userClient.rejectLoan(approval.getLoanId());
        } catch (Exception e) {
            System.err.println("Note: User Service loan status not updated: " + e.getMessage());
        }
        
        loanApprovalRepository.delete(approval);
    }

    /**
     * Get approval summary for dashboard
     */
    public ApprovalSummary getApprovalSummary() {
        List<LoanApproval> all = loanApprovalRepository.findAll();
        long pending = all.stream().filter(a -> "PENDING".equals(a.getApprovalStatus())).count();
        long approved = all.stream().filter(a -> "APPROVED".equals(a.getApprovalStatus())).count();
        long rejected = all.stream().filter(a -> "REJECTED".equals(a.getApprovalStatus())).count();
        
        double totalLoanAmount = all.stream()
                .filter(a -> "APPROVED".equals(a.getApprovalStatus()))
                .mapToDouble(LoanApproval::getLoanAmount)
                .sum();

        return new ApprovalSummary(pending, approved, rejected, totalLoanAmount);
    }

    // Inner class for approval summary
    public static class ApprovalSummary {
        public long pendingCount;
        public long approvedCount;
        public long rejectedCount;
        public double totalApprovedLoanAmount;

        public ApprovalSummary(long pendingCount, long approvedCount, long rejectedCount, double totalApprovedLoanAmount) {
            this.pendingCount = pendingCount;
            this.approvedCount = approvedCount;
            this.rejectedCount = rejectedCount;
            this.totalApprovedLoanAmount = totalApprovedLoanAmount;
        }

        public long getPendingCount() {
            return pendingCount;
        }

        public long getApprovedCount() {
            return approvedCount;
        }

        public long getRejectedCount() {
            return rejectedCount;
        }

        public double getTotalApprovedLoanAmount() {
            return totalApprovedLoanAmount;
        }
    }
}
