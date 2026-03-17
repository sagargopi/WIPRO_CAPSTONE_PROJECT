package com.example.user.controller;

import com.example.user.modal.Investment;
import com.example.user.service.InvestmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/investments")
public class InvestmentController {

    @Autowired
    private InvestmentService investmentService;

    /**
     * Create a new investment
     */
    @PostMapping("/create")
    public Investment createInvestment(@RequestParam Long userId,
                                      @RequestParam Long accountId,
                                      @RequestParam String investmentType,
                                      @RequestParam Double amount,
                                      @RequestParam Integer tenureMonths) {
        return investmentService.createInvestment(userId, accountId, investmentType, amount, tenureMonths);
    }

    /**
     * Get all investments for a user
     */
    @GetMapping("/user/{userId}")
    public List<Investment> getUserInvestments(@PathVariable Long userId) {
        return investmentService.getUserInvestments(userId);
    }

    /**
     * Get investments by type
     */
    @GetMapping("/user/{userId}/type/{investmentType}")
    public List<Investment> getInvestmentsByType(@PathVariable Long userId,
                                                @PathVariable String investmentType) {
        return investmentService.getInvestmentsByType(userId, investmentType);
    }

    /**
     * Get active investments for a user
     */
    @GetMapping("/user/{userId}/active")
    public List<Investment> getActiveInvestments(@PathVariable Long userId) {
        return investmentService.getActiveInvestments(userId);
    }

    /**
     * Get a specific investment
     */
    @GetMapping("/{investmentId}")
    public Investment getInvestment(@PathVariable Long investmentId,
                                   @RequestParam Long userId) {
        return investmentService.getInvestmentById(investmentId, userId);
    }

    /**
     * Calculate maturity amount
     */
    @PostMapping("/calculate-maturity")
    public Map<String, Object> calculateMaturityAmount(@RequestParam Double principal,
                                                       @RequestParam Double annualRate,
                                                       @RequestParam Integer tenureMonths) {
        Map<String, Object> response = new HashMap<>();
        try {
            double maturityAmount = investmentService.calculateMaturityAmount(principal, annualRate, tenureMonths);
            double returns = maturityAmount - principal;

            response.put("principal", principal);
            response.put("interest_rate", annualRate);
            response.put("tenure_months", tenureMonths);
            response.put("maturity_amount", maturityAmount);
            response.put("returns", returns);
            response.put("status", "success");
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * Maturity an investment
     */
    @PostMapping("/{investmentId}/maturity")
    public Investment maturityInvestment(@PathVariable Long investmentId,
                                        @RequestParam Long userId) {
        return investmentService.maturityInvestment(investmentId, userId);
    }

    /**
     * Get investment summary for user
     */
    @GetMapping("/user/{userId}/summary")
    public Map<String, Object> getInvestmentSummary(@PathVariable Long userId) {
        InvestmentService.InvestmentSummary summary = investmentService.getInvestmentSummary(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("total_invested", summary.getTotalInvested());
        response.put("total_expected_returns", summary.getTotalExpectedReturns());
        response.put("investment_count", summary.getInvestmentCount());
        
        return response;
    }
}
