package com.ecommerce.app.controller;

import com.ecommerce.app.dto.LoginRequest;
import com.ecommerce.app.dto.LoginResponse;
import com.ecommerce.app.dto.RegisterRequest;
import com.ecommerce.app.dto.RegisterResponse;
import com.ecommerce.app.dto.UnlockRequest;
import com.ecommerce.app.dto.UnlockResponse;
import com.ecommerce.app.exception.InvalidInputException;
import com.ecommerce.app.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        RegisterResponse response = authService.registerUser(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Register a new admin user
     * Admin-only endpoint (requires ADMIN role)
     * Allows existing admins to create additional admin accounts
     */
    @PostMapping("/register-admin")
    public ResponseEntity<RegisterResponse> registerAdmin(@RequestBody RegisterRequest request) {
        RegisterResponse response = authService.registerAdmin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.loginUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/unlock")
    public ResponseEntity<UnlockResponse> unlockAccount(@RequestBody UnlockRequest request) {
        // Validate email is not empty
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new InvalidInputException("Email is required");
        }

        // Validate email format (basic check)
        if (!request.getEmail().contains("@")) {
            throw new InvalidInputException("Invalid email format");
        }

        UnlockResponse response = authService.unlockAccount(request.getEmail());
        return ResponseEntity.ok(response);
    }
}
