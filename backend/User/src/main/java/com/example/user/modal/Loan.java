package com.example.user.modal;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    @JsonProperty("userId")
    private Long userId;

    @Column(name = "account_id", nullable = false)
    @JsonProperty("accountId")
    private Long accountId;

    @Column(name = "loan_amount", nullable = false)
    @JsonProperty("loanAmount")
    private Double loanAmount;

    @Column(name = "interest_rate", nullable = false)
    @JsonProperty("interestRate")
    private Double interestRate;

    @Column(name = "loan_tenure_months", nullable = false)
    @JsonProperty("loanTenure")
    private Integer loanTenureMonths;

    @Column(name = "monthly_emi")
    @JsonProperty("monthlyEmi")
    private Double monthlyEmi;

    @Column(name = "loan_status")
    @JsonProperty("loanStatus")
    private String loanStatus = "PENDING";

    @Column(name = "loan_category")
    @JsonProperty("loanType")
    private String loanCategory = "Personal";

    @Column(name = "visibility")
    @JsonProperty("visibility")
    private Boolean visibility = true;

    @Column(name = "rating")
    @JsonProperty("rating")
    private Integer rating = 0;

    @Column(name = "user_comments", length = 500)
    @JsonProperty("userComments")
    private String userComments;

    @Column(name = "admin_comments", length = 500)
    @JsonProperty("adminComments")
    private String adminComments;

    @Column(name = "application_date")
    private LocalDateTime applicationDate = LocalDateTime.now();

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Loan(Long userId, Long accountId, Double loanAmount, Double interestRate, Integer loanTenureMonths) {
        this.userId = userId;
        this.accountId = accountId;
        this.loanAmount = loanAmount;
        this.interestRate = interestRate;
        this.loanTenureMonths = loanTenureMonths;
        this.loanStatus = "PENDING";
        this.applicationDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
