package com.example.owner.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "user-service", url = "http://localhost:8082")
public interface NotificationClient {

    @PostMapping("/api/notifications/loan-update")
    void sendLoanNotification(
        @RequestParam("userId") Long userId, 
        @RequestParam("message") String message,
        @RequestParam("loanId") Long loanId
    );
}