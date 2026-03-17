package com.example.owner.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.owner.modal.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByOwnerId(Long ownerId);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Finds notifications for the admin/owner
    List<Notification> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    void deleteByUserId(Long userId);
}