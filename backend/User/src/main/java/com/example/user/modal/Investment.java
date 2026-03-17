package com.example.user.modal;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "investments")
@Data
@NoArgsConstructor
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "investment_type", nullable = false)
    private String investmentType;  // FIXED_DEPOSIT, RECURRING_DEPOSIT, LOAN_INVESTMENT

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "tenure_months")
    private Integer tenureMonths;

    @Column(name = "interest_rate")
    private Double interestRate = 0.0;

    @Column(name = "investment_status")
    private String investmentStatus = "ACTIVE";

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Investment(Long userId, Long accountId, String investmentType, Double amount, Integer tenureMonths, Double interestRate) {
        this.userId = userId;
        this.accountId = accountId;
        this.investmentType = investmentType;
        this.amount = amount;
        this.tenureMonths = tenureMonths;
        this.interestRate = interestRate;
        this.investmentStatus = "ACTIVE";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
