package com.example.owner.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.owner.client.UserClient;
import com.example.owner.modal.LoanApproval;
import com.example.owner.service.LoanApprovalService;
import com.example.owner.service.NotificationService;

@RestController
@RequestMapping("/loan-approvals")
public class LoanApprovalController {

    @Autowired
    private LoanApprovalService loanApprovalService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserClient userClient;

    /**
     * Create a new loan approval record AND notify the admin
     * Triggered by User Service via Feign Client
     */
    @PostMapping("/create")
    public ResponseEntity<LoanApproval> createLoanApproval(
            @RequestParam Long loanId,
            @RequestParam Long userId,
            @RequestParam Double loanAmount,
            @RequestParam Double interestRate,
            @RequestParam Integer tenureMonths,
            @RequestParam Double monthlyEmi,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String userComments) {
        
        // 1. Save the loan record for the Admin's pending list
        LoanApproval approval = loanApprovalService.createLoanApprovalRecord(
                loanId, userId, loanAmount, interestRate, tenureMonths, monthlyEmi, category, userComments
        );

        // 3. TRIGGER NOTIFICATION: Send alert to Admin (Owner ID 1)
        try {
            String adminMsg = "New Loan Application #" + loanId + " received from User ID: " + userId;
            // Using your existing System Notification method
            notificationService.createLoanStatusNotification(userId, 1L, adminMsg, loanId);
        } catch (Exception e) {
            System.err.println("⚠️ Admin notification alert failed: " + e.getMessage());
        }

        return ResponseEntity.ok(approval);
    }


    // Add to Owner/src/main/java/com/example/owner/controller/LoanApprovalController.java

    @DeleteMapping("/{approvalId}")
    public ResponseEntity<?> deleteLoan(@PathVariable Long approvalId) {
        try {
            loanApprovalService.deleteLoanApproval(approvalId);
            return ResponseEntity.ok(Map.of("message", "Loan request deleted directly."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

     // Get all pending loan approvals

    @GetMapping("/pending")
    public List<LoanApproval> getPendingApprovals() {
        return loanApprovalService.getPendingApprovals();
    }


     // Get pending approvals for a specific user

    @GetMapping("/user/{userId}/pending")
    public List<LoanApproval> getPendingApprovalsForUser(@PathVariable Long userId) {
        return loanApprovalService.getPendingApprovalsForUser(userId);
    }


     // Get all approvals for a user

    @GetMapping("/user/{userId}")
    public List<LoanApproval> getUserApprovals(@PathVariable Long userId) {
        return loanApprovalService.getUserApprovals(userId);
    }

     //Get a specific approval

    @GetMapping("/{approvalId}")
    public LoanApproval getApprovalById(@PathVariable Long approvalId) {
        return loanApprovalService.getApprovalById(approvalId);
    }

 
     // Get approvals by status

    @GetMapping("/status/{status}")
    public List<LoanApproval> getApprovalsByStatus(@PathVariable String status) {
        return loanApprovalService.getApprovalsByStatus(status);
    }

    /**
     * Requirement: Visibility control, rating, and comments update for applications
     */
    @PutMapping("/{loanId}/details")
    public ResponseEntity<Void> updateLoanDetails(@PathVariable Long loanId,
                                                  @RequestParam(required = false) Boolean visibility,
                                                  @RequestParam(required = false) Integer rating,
                                                  @RequestParam(required = false) String adminComments) {
        // Forward update to User service where the primary Loan record exists
        userClient.updateLoanDetails(loanId, visibility, rating, adminComments);
        return ResponseEntity.ok().build();
    }

    //Approve a loan application
    @PostMapping("/{approvalId}/approve")
    public LoanApproval approveLoan(@PathVariable Long approvalId,
                                   @RequestParam Long adminId,
                                   @RequestParam(required = false, defaultValue = "") String comments) {
        return loanApprovalService.approveLoan(approvalId, adminId, comments);
    }

    // Reject a loan application
    @PostMapping("/{approvalId}/reject")
    public LoanApproval rejectLoan(@PathVariable Long approvalId,
                                  @RequestParam Long adminId,
                                  @RequestParam(required = false, defaultValue = "") String comments) {
        return loanApprovalService.rejectLoan(approvalId, adminId, comments);
    }

    // Get approval summary for dashboard
    @GetMapping("/summary")
    public Map<String, Object> getApprovalSummary() {
        LoanApprovalService.ApprovalSummary summary = loanApprovalService.getApprovalSummary();
        
        Map<String, Object> response = new HashMap<>();
        response.put("pending_count", summary.getPendingCount());
        response.put("approved_count", summary.getApprovedCount());
        response.put("rejected_count", summary.getRejectedCount());
        response.put("total_approved_loan_amount", summary.getTotalApprovedLoanAmount());
        
        return response;
    }
}
