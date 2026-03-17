package com.example.user.modal;

/**
 * Enum for notification types
 * Represents different categories of notifications sent to users
 */
public enum NotificationType {
    DOWNLOAD_TRACKING,
    BALANCE_ALERT,    // When account balance reaches zero or critical level
    LOAN_STATUS,      // When loan application status changes (approved/rejected)
    TRANSACTION,      // When transaction is completed or failed
    SYSTEM            // General system announcements and updates
}
