package com.example.user.dto;

import lombok.Data;

@Data
public class LoanRequestDTO {
    private Long userId;
    private Double loanAmount;
    private Double interestRate;
    private Integer loanTenure; // matches 'loanTenure' in React
    private String loanType;    // matches 'loanType' in React
    private String userComments;
}
