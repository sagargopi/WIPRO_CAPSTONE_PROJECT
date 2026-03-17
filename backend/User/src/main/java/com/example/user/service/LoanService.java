package com.example.user.service;

import com.example.user.client.OwnerClient;
import com.example.user.modal.Loan;
import com.example.user.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private OwnerClient ownerClient;

    /**
     * Calculate EMI (Equated Monthly Installment)
     * Formula: EMI = P * R * (1 + R)^N / ((1 + R)^N - 1)
     * Where:
     * P = Principal amount
     * R = Monthly interest rate (Annual rate / 12 / 100)
     * N = Number of months
     */
    public double calculateEMI(Double principal, Double annualInterestRate, Integer tenureMonths) {
        if (principal <= 0 || annualInterestRate < 0 || tenureMonths <= 0) {
            throw new IllegalArgumentException("Invalid loan parameters");
        }

        double monthlyRate = annualInterestRate / 12 / 100;

        // If interest rate is 0, simple division
        if (monthlyRate == 0) {
            return principal / tenureMonths;
        }

        double numerator = principal * monthlyRate * Math.pow(1 + monthlyRate, tenureMonths);
        double denominator = Math.pow(1 + monthlyRate, tenureMonths) - 1;

        return numerator / denominator;
    }

    /**
     * Apply for a new loan
     */
    public Loan applyForLoan(Long userId, Long accountId, Double loanAmount, Double interestRate, Integer tenureMonths, String category, String userComments) {
        if (loanAmount <= 0) {
            throw new IllegalArgumentException("Loan amount must be positive");
        }

        // If accountId is not provided, look it up via userId
        if (accountId == null) {
            com.example.user.modal.Account account = accountService.getAccountByUserId(userId);
            if (account == null) {
                throw new RuntimeException("Account not found for User: " + userId);
            }
            accountId = account.getId();
        }

        Loan loan = new Loan(userId, accountId, loanAmount, interestRate, tenureMonths);
        loan.setLoanCategory(category != null ? category : "Personal");
        loan.setUserComments(userComments);
        loan.setVisibility(true);
        loan.setRating(0);
        
        // Calculate EMI
        double emi = calculateEMI(loanAmount, interestRate, tenureMonths);
        loan.setMonthlyEmi(emi);
        
        loan.setLoanStatus("PENDING");

        // Save loan in User service
        Loan savedLoan = loanRepository.save(loan);

        // Create loan approval record in Owner service
        try {
            ownerClient.createLoanApproval(
                savedLoan.getId(),
                userId,
                loanAmount,
                interestRate,
                tenureMonths,
                emi,
                savedLoan.getLoanCategory(),
                savedLoan.getUserComments()
            );
        } catch (Exception e) {
            System.err.println("Failed to create loan approval record in Owner service: " + e.getMessage());
        }

        return savedLoan;
    }

    /**
     * Get filtered loans for a user (Search & Filter)
     */
    public List<Loan> getFilteredLoans(Long userId, String category, String searchTerm) {
        if (userId != null) {
            return loanRepository.findByUserId(userId);
        }
        
        if (category != null && !category.equals("All") && searchTerm != null && !searchTerm.isEmpty()) {
            return loanRepository.findByLoanCategoryContainingOrUserCommentsContaining(searchTerm, searchTerm)
                    .stream().filter(l -> l.getLoanCategory().equals(category)).toList();
        } else if (category != null && !category.equals("All")) {
            return loanRepository.findByLoanCategory(category);
        } else if (searchTerm != null && !searchTerm.isEmpty()) {
            return loanRepository.findByLoanCategoryContainingOrUserCommentsContaining(searchTerm, searchTerm);
        }
        
        return loanRepository.findAll();
    }

    /**
     * Update loan visibility, rating, or comments (for Rubric)
     */
    public Loan updateLoanDetails(Long loanId, Boolean visibility, Integer rating, String comments, boolean isAdmin) {
        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new RuntimeException("Loan not found"));
        if (visibility != null) loan.setVisibility(visibility);
        if (rating != null) loan.setRating(rating);
        if (comments != null) {
            if (isAdmin) {
                loan.setAdminComments(comments);
            } else {
                loan.setUserComments(comments);
            }
        }
        loan.setUpdatedAt(java.time.LocalDateTime.now());
        return loanRepository.save(loan);
    }

    /**
     * Get all loans for a user (Respecting visibility)
     */
    public List<Loan> getUserLoans(Long userId) {
        return loanRepository.findByUserId(userId).stream()
                .filter(loan -> loan.getVisibility() != null && loan.getVisibility())
                .toList();
    }

    /**
     * Get loans by status
     */
    public List<Loan> getLoansByStatus(String status) {
        return loanRepository.findByLoanStatus(status);
    }

    /**
     * Get a specific loan
     */
    public Loan getLoanById(Long loanId, Long userId) {
        return loanRepository.findByIdAndUserId(loanId, userId);
    }

    /**
     * Get pending loans for a user
     */
    public List<Loan> getPendingLoans(Long userId) {
        return loanRepository.findByUserIdAndLoanStatus(userId, "PENDING");
    }

    /**
     * Approve a loan (called by admin/owner service)
     */
    public Loan approveLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        
        loan.setLoanStatus("APPROVED");
        loan.setApprovalDate(java.time.LocalDate.now());
        
        // Add loan amount to user's account
        accountService.deposit(loan.getUserId(), loan.getLoanAmount());
        
        return loanRepository.save(loan);
    }

    /**
     * Reject a loan (called by admin/owner service)
     */
    public Loan rejectLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        
        loan.setLoanStatus("REJECTED");
        
        return loanRepository.save(loan);
    }

    /**
     * Get all pending loans across all users
     */
    public List<Loan> getAllPendingLoans() {
        return loanRepository.findByLoanStatus("PENDING");
    }

    /**
     * Calculate total interest payable
     */
    public double calculateTotalInterest(Double loanAmount, Double monthlyEmi, Integer tenureMonths) {
        return (monthlyEmi * tenureMonths) - loanAmount;
    }
}
