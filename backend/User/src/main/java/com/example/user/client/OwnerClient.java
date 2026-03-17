package com.example.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "owner-service", url = "http://localhost:8083")
public interface OwnerClient {

    /**
     * Create a loan approval record in Owner service when a loan is applied
     */
    @PostMapping("/loan-approvals/create")
    void createLoanApproval(@RequestParam Long loanId,
                           @RequestParam Long userId,
                           @RequestParam Double loanAmount,
                           @RequestParam Double interestRate,
                           @RequestParam Integer tenureMonths,
                           @RequestParam Double monthlyEmi,
                           @RequestParam String category,
                           @RequestParam String userComments);

    /**
     * Bridge for Zero Balance Alerts
     * This is the method your error log was looking for!
     */
    @PostMapping("/api/notifications/balance-alert")
    void sendAdminNotification(@RequestParam("message") String message, 
                               @RequestParam("userId") Long userId);
}
