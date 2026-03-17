package com.example.owner.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.owner.modal.Owner;

public interface OwnerRepository extends JpaRepository<Owner, Long> {

    Owner findByUsername(String username);

    boolean existsByUsername(String username);

}