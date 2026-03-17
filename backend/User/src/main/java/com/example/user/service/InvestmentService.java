package com.example.user.service;

import com.example.user.modal.Investment;
import com.example.user.repository.InvestmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class InvestmentService {

    @Autowired
    private InvestmentRepository investmentRepository;

    @Autowired
    private AccountService accountService;

    // Interest rates for different investment types (annual percentage)
    private static final double FIXED_DEPOSIT_RATE = 6.5;
    private static final double RECURRING_DEPOSIT_RATE = 5.5;
    private static final double LOAN_INVESTMENT_RATE = 8.0;

    /**
     * Create a new investment
     */
    public Investment createInvestment(Long userId, Long accountId, String investmentType, Double amount, Integer tenureMonths) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Investment amount must be positive");
        }

        // Check if user has sufficient balance
        double balance = accountService.getBalance(userId);
        if (balance < amount) {
            throw new IllegalArgumentException("Insufficient balance for investment");
        }

        // Get interest rate based on investment type
        double interestRate = getInterestRateForType(investmentType);

        Investment investment = new Investment(userId, accountId, investmentType, amount, tenureMonths, interestRate);

        // Set maturity date
        if (tenureMonths != null) {
            investment.setMaturityDate(LocalDate.now().plusMonths(tenureMonths));
        }

        // Deduct amount from account balance
        accountService.withdraw(userId, amount);

        return investmentRepository.save(investment);
    }

    /**
     * Get interest rate for investment type
     */
    private double getInterestRateForType(String investmentType) {
        return switch (investmentType.toUpperCase()) {
            case "FIXED_DEPOSIT" -> FIXED_DEPOSIT_RATE;
            case "RECURRING_DEPOSIT" -> RECURRING_DEPOSIT_RATE;
            case "LOAN_INVESTMENT" -> LOAN_INVESTMENT_RATE;
            default -> 0.0;
        };
    }

    /**
     * Calculate maturity amount for an investment
     * MA = P + (P * R * T / 100)
     * Where P = Principal, R = Rate, T = Time in years
     */
    public double calculateMaturityAmount(Double principal, Double annualRate, Integer tenureMonths) {
        if (principal <= 0 || annualRate < 0 || tenureMonths <= 0) {
            throw new IllegalArgumentException("Invalid investment parameters");
        }

        double timeInYears = tenureMonths / 12.0;
        double interest = (principal * annualRate * timeInYears) / 100;

        return principal + interest;
    }

    /**
     * Get all investments for a user
     */
    public List<Investment> getUserInvestments(Long userId) {
        return investmentRepository.findByUserId(userId);
    }

    /**
     * Get investments by type
     */
    public List<Investment> getInvestmentsByType(Long userId, String investmentType) {
        return investmentRepository.findByUserIdAndInvestmentType(userId, investmentType);
    }

    /**
     * Get all active investments for a user
     */
    public List<Investment> getActiveInvestments(Long userId) {
        List<Investment> allInvestments = investmentRepository.findByUserId(userId);
        return allInvestments.stream()
                .filter(inv -> "ACTIVE".equals(inv.getInvestmentStatus()))
                .toList();
    }

    /**
     * Get a specific investment
     */
    public Investment getInvestmentById(Long investmentId, Long userId) {
        return investmentRepository.findByIdAndUserId(investmentId, userId);
    }

    /**
     * Maturity an investment (transfer amount back to account)
     */
    public Investment maturityInvestment(Long investmentId, Long userId) {
        Investment investment = investmentRepository.findByIdAndUserId(investmentId, userId);
        if (investment == null) {
            throw new RuntimeException("Investment not found");
        }

        // Calculate maturity amount
        double maturityAmount = calculateMaturityAmount(investment.getAmount(), investment.getInterestRate(), investment.getTenureMonths());

        // Transfer maturity amount to account
        accountService.deposit(userId, maturityAmount);

        // Mark as matured
        investment.setInvestmentStatus("MATURED");

        return investmentRepository.save(investment);
    }

    /**
     * Calculate total returns
     */
    public double calculateReturns(Double principal, Double annualRate, Integer tenureMonths) {
        double maturityAmount = calculateMaturityAmount(principal, annualRate, tenureMonths);
        return maturityAmount - principal;
    }

    /**
     * Get investment summary for user
     */
    public InvestmentSummary getInvestmentSummary(Long userId) {
        List<Investment> investments = getUserInvestments(userId);
        
        double totalInvested = 0;
        double totalExpectedReturns = 0;

        for (Investment inv : investments) {
            totalInvested += inv.getAmount();
            if ("ACTIVE".equals(inv.getInvestmentStatus()) && inv.getTenureMonths() != null) {
                double returns = calculateReturns(inv.getAmount(), inv.getInterestRate(), inv.getTenureMonths());
                totalExpectedReturns += returns;
            }
        }

        return new InvestmentSummary(totalInvested, totalExpectedReturns, investments.size());
    }

    // Inner class for investment summary
    public static class InvestmentSummary {
        public double totalInvested;
        public double totalExpectedReturns;
        public int investmentCount;

        public InvestmentSummary(double totalInvested, double totalExpectedReturns, int investmentCount) {
            this.totalInvested = totalInvested;
            this.totalExpectedReturns = totalExpectedReturns;
            this.investmentCount = investmentCount;
        }

        public double getTotalInvested() {
            return totalInvested;
        }

        public double getTotalExpectedReturns() {
            return totalExpectedReturns;
        }

        public int getInvestmentCount() {
            return investmentCount;
        }
    }
}
