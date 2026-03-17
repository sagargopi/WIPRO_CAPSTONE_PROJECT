package com.example.owner.controller;

import com.example.owner.dto.LoginRequestDTO;
import com.example.owner.dto.LoginResponseDTO;
import com.example.owner.dto.AccountDTO; // We use the DTO here
import com.example.owner.client.UserClient; // We use the Client here
import com.example.owner.dto.OwnerDTO;
import com.example.owner.dto.UserDTO;
import com.example.owner.modal.Owner;
import com.example.owner.service.OwnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/owner")
public class OwnerController {

    @Autowired
    private OwnerService ownerService;

    @Autowired
    private UserClient userClient;

    @PostMapping("/register")
    public ResponseEntity<OwnerDTO> register(@RequestBody Owner owner){
        OwnerDTO registeredOwner = ownerService.register(owner);
        return new ResponseEntity<>(registeredOwner, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO response = ownerService.login(loginRequest.getUsername(), loginRequest.getPassword());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<OwnerDTO>> getOwners(){
        List<OwnerDTO> owners = ownerService.getOwners();
        return new ResponseEntity<>(owners, HttpStatus.OK);
    }
    @PutMapping("/users/{userId}")
    public ResponseEntity<UserDTO> updateCustomer(@PathVariable Long userId,
                                                  @RequestBody Map<String, Object> updates) {
        UserDTO updated = userClient.updateUser(userId, updates);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOwner(@PathVariable Long id){
        ownerService.deleteOwner(id);
        return new ResponseEntity<>("Owner deleted successfully", HttpStatus.OK);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id,
                                           @RequestHeader("Authorization") String token) {
        try {
            ownerService.deleteCustomer(token, id);
            return ResponseEntity.ok(Map.of("message", "Customer deactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", "Deletion failed: " + e.getMessage()));
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<OwnerDTO> updateOwner(@PathVariable Long id, @RequestBody Owner ownerDetails){
        OwnerDTO updatedOwner = ownerService.updateOwner(id, ownerDetails);
        return new ResponseEntity<>(updatedOwner, HttpStatus.OK);
    }
    
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getUsers(){
        List<UserDTO> users = ownerService.getUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    /**
     * Requirement: Admin/Owner CRUD for adding user details
     */
    @PostMapping("/users")
    public ResponseEntity<UserDTO> addCustomer(@RequestBody Map<String, Object> userData) {
        UserDTO newUser = userClient.registerUser(userData);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    @GetMapping("/accounts/user/{userId}")
    public ResponseEntity<?> getAccountByUserId(@PathVariable Long userId, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("message", "Authorization header is required"));
            }

            Map<String, Object> accountDetails = userClient.getAccountDetails(userId, authorizationHeader);

            if (accountDetails == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Account not found for user"));
            }
            return ResponseEntity.ok(accountDetails);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error fetching account: " + e.getMessage()));
        }
    }

    
}
