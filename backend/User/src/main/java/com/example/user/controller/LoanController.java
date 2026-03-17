package com.example.user.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.user.dto.LoanRequestDTO;
import com.example.user.modal.Loan;
import com.example.user.service.LoanService;

@RestController
@RequestMapping("/loans")
@CrossOrigin(origins = "http://localhost:5173")
public class LoanController {

    @Autowired
    private LoanService loanService;

    /**
     * Apply for a new loan
     * Using @RequestBody with LoanRequestDTO to match frontend payload
     */
    @PostMapping("/apply")
    public Loan applyForLoan(@RequestBody LoanRequestDTO request) {
        // We'll find the accountId in the service using the userId
        return loanService.applyForLoan(
                request.getUserId(), 
                null, // accountId will be looked up in the service
                request.getLoanAmount(), 
                request.getInterestRate(), 
                request.getLoanTenure(), 
                request.getLoanType(), 
                request.getUserComments()
        );
    }

    /**
     * Search and Filter loans (for Rubric: Category/Rating filtering & keyword search)
     */
    @GetMapping("/search")
    public List<Loan> searchLoans(@RequestParam(required = false) String category,
                                  @RequestParam(required = false) String searchTerm) {
        return loanService.getFilteredLoans(null, category, searchTerm);
    }

    /**
     * Update loan details (Visibility, Rating, Admin Comments)
     */
    @PutMapping("/{loanId}/details")
    public Loan updateLoanDetails(@PathVariable Long loanId,
                                  @RequestParam(required = false) Boolean visibility,
                                  @RequestParam(required = false) Integer rating,
                                  @RequestParam(required = false) String adminComments) {
        return loanService.updateLoanDetails(loanId, visibility, rating, adminComments, true);
    }

    /**
     * Calculate EMI for loan
     */
    @PostMapping("/calculate-emi")
    public Map<String, Object> calculateEMI(@RequestParam Double principal,
                                           @RequestParam Double annualInterestRate,
                                           @RequestParam Integer tenureMonths) {
        Map<String, Object> response = new HashMap<>();
        try {
            double emi = loanService.calculateEMI(principal, annualInterestRate, tenureMonths);
            double totalPayable = emi * tenureMonths;
            double totalInterest = totalPayable - principal;

            response.put("monthly_emi", emi);
            response.put("total_payable", totalPayable);
            response.put("total_interest", totalInterest);
            response.put("status", "success");
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * Get all loans for a user
     */
    @GetMapping("/user/{userId}")
    public List<Loan> getUserLoans(@PathVariable Long userId) {
        return loanService.getUserLoans(userId);
    }

    /**
     * Get pending loans for a user
     */
    @GetMapping("/user/{userId}/pending")
    public List<Loan> getPendingLoans(@PathVariable Long userId) {
        return loanService.getPendingLoans(userId);
    }

    /**
     * Get a specific loan
     */
    @GetMapping("/{loanId}")
    public Loan getLoan(@PathVariable Long loanId,
                        @RequestParam Long userId) {
        return loanService.getLoanById(loanId, userId);
    }

    /**
     * Add user review/comment to a loan application
     */
    @PutMapping("/{loanId}/review")
    public Loan submitUserReview(@PathVariable Long loanId,
                                 @RequestParam String review) {
        return loanService.updateLoanDetails(loanId, null, null, review, false);
    }

    /**
     * Get loans by status
     */
    @GetMapping("/status/{status}")
    public List<Loan> getLoansByStatus(@PathVariable String status) {
        return loanService.getLoansByStatus(status);
    }

    /**
     * Get all pending loans (for admin)
     */
    @GetMapping("/admin/pending")
    public List<Loan> getAllPendingLoans() {
        return loanService.getAllPendingLoans();
    }

    /**
     * Approve a loan (admin only)
     */
    @PostMapping("/{loanId}/approve")
    public Loan approveLoan(@PathVariable Long loanId) {
        return loanService.approveLoan(loanId);
    }

    /**
     * Reject a loan (admin only)
     */
    @PostMapping("/{loanId}/reject")
    public Loan rejectLoan(@PathVariable Long loanId) {
        return loanService.rejectLoan(loanId);
    }
}
