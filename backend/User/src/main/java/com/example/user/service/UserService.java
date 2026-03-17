package com.example.user.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.user.dto.LoginResponseDTO;
import com.example.user.dto.UserDTO;
import com.example.user.exception.InvalidCredentialsException;
import com.example.user.exception.UserAlreadyExistsException;
import com.example.user.exception.UserNotFoundException;
import com.example.user.modal.Account;
import com.example.user.modal.User;
import com.example.user.repository.AccountRepository;
import com.example.user.repository.InvestmentRepository;
import com.example.user.repository.LoanRepository;
import com.example.user.repository.MessageRepository;
import com.example.user.repository.NotificationRepository;
import com.example.user.repository.TransactionRepository;
import com.example.user.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private InvestmentRepository investmentRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private JWTService jwtService;

    public UserDTO register(User user){
    	
    	if(userRepository.existsByEmail(user.getEmail())){
            throw new UserAlreadyExistsException("Email already registered");
        }

        User savedUser = userRepository.save(user);

        Account account = new Account();

        account.setUserId(savedUser.getId());

        account.setBalance(0);

        String accountNumber = "ACC" + System.currentTimeMillis();

        account.setAccountNumber(accountNumber);

        accountRepository.save(account);

        return convertToUserDTO(savedUser);
    }

   // File: User/src/main/java/com/example/user/service/UserService.java

    @Transactional
    public void deleteUserById(Long userId) {
        // Soft delete: Deactivate the user instead of removing from DB
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.setActive(false);
        userRepository.save(user);
        
        System.out.println("Successfully deactivated user " + userId + ". Records kept for future use.");
    }
        
    public String deleteUserByEmail(String email) {

        User user = userRepository.findByEmail(email);

        if(user == null){
            throw new UserNotFoundException("User not found");
        }

        accountRepository.deleteByUserId(user.getId());

        userRepository.delete(user);

        return "User deleted successfully";
    }
    
    @Transactional
    public String deleteUser(String email){

        User user = userRepository.findByEmail(email);

        if(user == null){
            throw new UserNotFoundException("User not found");
        }

        accountRepository.deleteByUserId(user.getId());

        userRepository.delete(user);

        return "User and Account deleted successfully";
    }
    
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }

    public LoginResponseDTO login(String username, String password) {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UserNotFoundException("User not found");
        }

        if (!user.getPassword().equals(password)) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (user.getActive() != null && !user.getActive()) {
            throw new InvalidCredentialsException("Account is deactivated. Please contact support.");
        }

        // Fetch user's account
        Account account = accountRepository.findByUserId(user.getId());
        if (account == null) {
            throw new RuntimeException("Account not found for user");
        }

        String token = jwtService.generateToken(username, user.getRole());
        
        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setUserId(user.getId());
        response.setAccountId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setBalance(account.getBalance());

        return response;
    }

    public User getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        return user;
    }

    public UserDTO getUserDTOByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        return convertToUserDTO(user);
    }

    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.getActive());
        
        // Fetch and set account number
        Account account = accountRepository.findByUserId(user.getId());
        if (account != null) {
            dto.setAccountNumber(account.getAccountNumber());
            System.out.println("✅ Account found for user " + user.getUsername() + ": " + account.getAccountNumber());
        } else {
            System.out.println("⚠️ No account found for user " + user.getUsername());
            dto.setAccountNumber(null);
        }
        
        return dto;
    }

    // File: User/src/main/java/com/example/user/service/UserService.java

    @Transactional
    public UserDTO updateUserCredentials(Long userId, Map<String, Object> updateData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Update Username if provided
        if (updateData.containsKey("username")) {
            user.setUsername((String) updateData.get("username"));
        }

        // Update Password if provided
        if (updateData.containsKey("password") && !((String) updateData.get("password")).isEmpty()) {
            user.setPassword((String) updateData.get("password"));
        }

        User savedUser = userRepository.save(user);
        return convertToUserDTO(savedUser);
    }

    public String deleteUser(Long id){

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        accountRepository.deleteByUserId(user.getId());

        userRepository.delete(user);

        return "User deleted successfully";
    }

    /**
     * Logic to update status specifically for the Admin/Owner request
     */
    @Transactional
    public UserDTO updateUserStatus(Long userId, Map<String, Object> updateData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (updateData.containsKey("username")) {
            Object usernameValue = updateData.get("username");
            if (usernameValue instanceof String username && !username.isBlank()) {
                user.setUsername(username);
            }
        }

        if (updateData.containsKey("password")) {
            Object passwordValue = updateData.get("password");
            if (passwordValue instanceof String password && !password.isBlank()) {
                user.setPassword(password);
            }
        }

        // Check if the map contains the 'isActive' key
        if (updateData.containsKey("isActive")) {
            // Safe conversion of the value to Boolean
            Object statusValue = updateData.get("isActive");
            if (statusValue instanceof Boolean) {
                user.setActive((Boolean) statusValue);
            } else if (statusValue instanceof String) {
                user.setActive(Boolean.parseBoolean((String) statusValue));
            }
            
            System.out.println("User ID: " + userId + " updated to active status: " + user.getActive());
        }

        User savedUser = userRepository.save(user);
        return convertToUserDTO(savedUser);
    }
}
