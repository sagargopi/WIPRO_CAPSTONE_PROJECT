package com.example.owner.modal;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loan_approvals")
@Data
@NoArgsConstructor
public class LoanApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_id", nullable = false)
    @JsonProperty("loanId")
    private Long loanId;

    @Column(name = "user_id", nullable = false)
    @JsonProperty("userId")
    private Long userId;

    @Column(name = "loan_amount", nullable = false)
    @JsonProperty("loanAmount")
    private Double loanAmount;

    @Column(name = "interest_rate", nullable = false)
    @JsonProperty("interestRate")
    private Double interestRate;

    @Column(name = "tenure_months", nullable = false)
    @JsonProperty("tenureMonths")
    private Integer tenureMonths;

    @Column(name = "monthly_emi")
    @JsonProperty("monthlyEmi")
    private Double monthlyEmi;

    @Column(name = "loan_category")
    @JsonProperty("loanCategory")
    private String loanCategory;

    @Column(name = "user_comments", length = 500)
    @JsonProperty("userComments")
    private String userComments;

    @Column(name = "approval_status")
    @JsonProperty("approvalStatus")
    private String approvalStatus = "PENDING";

    @Column(name = "reviewed_by")
    @JsonProperty("reviewedBy")
    private Long reviewedBy;

    @Column(name = "review_date")
    @JsonProperty("reviewDate")
    private LocalDate reviewDate;

    @Column(name = "comments")
    @JsonProperty("comments")
    private String comments;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public LoanApproval(Long loanId, Long userId, Double loanAmount, Double interestRate, Integer tenureMonths) {
        this.loanId = loanId;
        this.userId = userId;
        this.loanAmount = loanAmount;
        this.interestRate = interestRate;
        this.tenureMonths = tenureMonths;
        this.approvalStatus = "PENDING";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
