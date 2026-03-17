package com.example.user.controller;

import com.example.user.dto.LoginRequestDTO;
import com.example.user.dto.LoginResponseDTO;
import com.example.user.dto.UserDTO;
import com.example.user.exception.UserNotFoundException;
import com.example.user.modal.User;
import com.example.user.service.UserService;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody User user) {
        UserDTO registeredUser = userService.register(user);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest){
        LoginResponseDTO response = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    /**
     * SINGLE UPDATE ENDPOINT
     * This method handles both standard user profile updates 
     * AND the Admin's status (Active/Suspended) updates via the Owner Service.
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody Map<String, Object> updateData) {
        try {
            UserDTO updatedUser = userService.updateUserStatus(userId, updateData);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Update failed: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUserById(@PathVariable Long userId) {
        try {
            userService.deleteUserById(userId);
            return ResponseEntity.ok(Map.of("message", "User account deactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Deletion failed: " + e.getMessage()));
        }
    }

    @GetMapping("/lookup")
    public ResponseEntity<?> getUserByUsername(@RequestParam String username) {
        try {
            // Return a richer DTO that includes account details needed for transfers
            UserDTO userDto = userService.getUserDTOByUsername(username);
            return ResponseEntity.ok(Map.of(
                "found", true,
                "user", userDto
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "found", false,
                "username", username
            ));
        }
    }
}
