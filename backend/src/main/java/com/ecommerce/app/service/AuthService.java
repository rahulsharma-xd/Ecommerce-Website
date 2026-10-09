package com.ecommerce.app.service;

import com.ecommerce.app.dto.LoginRequest;
import com.ecommerce.app.dto.LoginResponse;
import com.ecommerce.app.dto.RegisterRequest;
import com.ecommerce.app.dto.RegisterResponse;
import com.ecommerce.app.dto.UnlockResponse;
import com.ecommerce.app.entity.Role;
import com.ecommerce.app.entity.User;
import com.ecommerce.app.exception.InvalidCredentialsException;
import com.ecommerce.app.exception.InvalidInputException;
import com.ecommerce.app.exception.UserAlreadyExistsException;
import com.ecommerce.app.repository.UserRepository;
import com.ecommerce.app.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;



@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public RegisterResponse registerUser(RegisterRequest request) {
        // Validate input
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new InvalidInputException("Username is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new InvalidInputException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new InvalidInputException("Password is required");
        }

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already taken");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Save to database
        User savedUser = userRepository.save(user);

        // Create response
        return new RegisterResponse(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            "User registered successfully"
        );
    }

    public LoginResponse loginUser(LoginRequest request) {
        // Validate input
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new InvalidInputException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new InvalidInputException("Password is required");
        }


        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Check if account is locked FIRST (before checking password)
        if (user.getAccountLocked()) {
            throw new RuntimeException("Account is locked due to too many failed login attempts. Contact an administrator.");
        }

        // Check if password matches
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts()+1);
            userRepository.save(user);
            // Lock account after 5 failed attempts
            if (user.getFailedLoginAttempts() >= 5) {
                user.setAccountLocked(true);
                userRepository.save(user);
                throw new RuntimeException("Account locked !");
            }
            throw new InvalidCredentialsException("Invalid email or password. Failed attempts :  " + user.getFailedLoginAttempts());
        }

        // If password matches and account is not locked, reset failed attempts
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail());

        // Create response with token
        return new LoginResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole().toString(),
            "Login successful",
            token
        );
    }
    public UnlockResponse unlockAccount(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        return new UnlockResponse(
            user.getId(),
            user.getEmail(),
            "Account unlocked successfully",
            true
        );
    }

    /**
     * Register a new admin user
     * This method is called by an admin-only endpoint (requires ADMIN role)
     * Allows existing admins to create additional admin accounts
     */
    public RegisterResponse registerAdmin(RegisterRequest request) {
        // Validate input
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new InvalidInputException("Username is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new InvalidInputException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new InvalidInputException("Password is required");
        }

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already taken");
        }

        // Create new admin user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ADMIN); // Set role as ADMIN
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Save to database
        User savedUser = userRepository.save(user);

        // Create response
        return new RegisterResponse(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            "Admin registered successfully"
        );
    }
}
