package com.example.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.user.modal.User;

public interface UserRepository extends JpaRepository<User, Long>{

	User findByUsername(String username);
	
	 User findByEmail(String email);

	 boolean existsByEmail(String email);

	 void deleteByEmail(String email);
}
