package com.example.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.user.modal.Investment;
import java.util.List;

public interface InvestmentRepository extends JpaRepository<Investment, Long> {
    
    List<Investment> findByUserId(Long userId);
    
    List<Investment> findByInvestmentType(String investmentType);
    
    List<Investment> findByUserIdAndInvestmentType(Long userId, String investmentType);
    
    List<Investment> findByInvestmentStatus(String investmentStatus);
    
    Investment findByIdAndUserId(Long id, Long userId);

    void deleteByUserId(Long userId);
}
