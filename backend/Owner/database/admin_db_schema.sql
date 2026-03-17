-- Owner/Admin Service Database Schema

CREATE DATABASE IF NOT EXISTS admin_db;
USE admin_db;

-- Owners Table
CREATE TABLE IF NOT EXISTS owners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'ADMIN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Loan Approvals Table
CREATE TABLE IF NOT EXISTS loan_approvals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    loan_amount DOUBLE NOT NULL,
    interest_rate DOUBLE NOT NULL,
    tenure_months INT NOT NULL,
    monthly_emi DOUBLE,
    approval_status VARCHAR(50) DEFAULT 'PENDING',
    reviewed_by BIGINT,
    review_date TIMESTAMP NULL,
    comments VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_loan_id (loan_id),
    INDEX idx_user_id (user_id),
    INDEX idx_approval_status (approval_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Messages/Chat Table
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    message_text VARCHAR(1000) NOT NULL,
    message_type VARCHAR(50) DEFAULT 'TEXT',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sender_id (sender_id),
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample admin data for testing
INSERT INTO owners (username, email, password, role) VALUES 
('Admin', 'admin@example.com', 'admin123', 'ADMIN'),
('admin1', 'admin1@example.com', 'admin123', 'ADMIN'),
('admin2', 'admin2@example.com', 'admin123', 'ADMIN');

-- Verify data
SELECT * FROM owners;
SELECT * FROM loan_approvals;
SELECT * FROM messages;
