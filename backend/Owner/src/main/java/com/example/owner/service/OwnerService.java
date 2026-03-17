package com.example.owner.service;

import com.example.owner.client.UserClient;
import com.example.owner.dto.LoginResponseDTO;
import com.example.owner.dto.OwnerDTO;
import com.example.owner.dto.UserDTO;
import com.example.owner.exception.InvalidCredentialsException;
import com.example.owner.exception.OwnerAlreadyExistsException;
import com.example.owner.exception.OwnerNotFoundException;
import com.example.owner.modal.Owner;
import com.example.owner.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OwnerService {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private LoanApprovalRepository loanApprovalRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JWTService jwtService;
    
    @Autowired
    private UserClient userClient;

    @Transactional
    public void deleteCustomer(String token, Long userId) {
        // Soft delete: Call User Service to deactivate the user
        userClient.deleteUser(token, userId);

        // We keep the local records (Loan Approvals, Messages) in Admin DB for audit history
        System.out.println("Admin Service: User " + userId + " has been deactivated in User Service.");
    }

    public OwnerDTO register(Owner owner) {

        if (ownerRepository.existsByUsername(owner.getUsername())) {
            throw new OwnerAlreadyExistsException("Username already exists");
        }

        Owner savedOwner = ownerRepository.save(owner);
        return convertToOwnerDTO(savedOwner);
    }

    public List<UserDTO> getUsers() {
        return userClient.getAllUsers();
    }

    public LoginResponseDTO login(String username, String password) {

        Owner owner = ownerRepository.findByUsername(username);

        if (owner == null) {
            throw new OwnerNotFoundException("Owner not found");
        }

        if (!owner.getPassword().equals(password)) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String token = jwtService.generateToken(username, "ADMIN");
        
        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        response.setUsername(owner.getUsername());
        response.setOwnerId(owner.getId());

        return response;
    }

    public List<OwnerDTO> getOwners() {
        return ownerRepository.findAll()
                .stream()
                .map(this::convertToOwnerDTO)
                .collect(Collectors.toList());
    }

    public void deleteOwner(Long id) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new OwnerNotFoundException("Owner not found"));
        ownerRepository.deleteById(id);
    }

    public OwnerDTO updateOwner(Long id, Owner ownerDetails) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new OwnerNotFoundException("Owner not found"));
        
        owner.setUsername(ownerDetails.getUsername());
        owner.setEmail(ownerDetails.getEmail());
        
        Owner updatedOwner = ownerRepository.save(owner);
        return convertToOwnerDTO(updatedOwner);
    }

    private OwnerDTO convertToOwnerDTO(Owner owner) {
        OwnerDTO dto = new OwnerDTO();
        dto.setId(owner.getId());
        dto.setUsername(owner.getUsername());
        return dto;
    }
}