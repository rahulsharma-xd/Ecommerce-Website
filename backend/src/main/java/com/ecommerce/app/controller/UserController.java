package com.ecommerce.app.controller;

import com.ecommerce.app.dto.RoleInfoResponse;
import com.ecommerce.app.dto.UserResponse;
import com.ecommerce.app.entity.User;
import com.ecommerce.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /**
     * USER-only endpoint
     * Only accessible by users with USER role
     * ADMIN role will receive 403 Forbidden
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user-only")
    public ResponseEntity<Map<String, String>> userOnly() {
        return ResponseEntity.ok(Map.of(
            "message", "Welcome! This endpoint is accessible only by USER role",
            "role", "USER"
        ));
    }

    /**
     * Dashboard endpoint for both USER and ADMIN roles
     * Accessible by both USER and ADMIN roles
     * Returns email and role information
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard(Authentication authentication) {
        // Extract email (username in our case)
        String email = authentication.getName();

        // Extract roles from authorities
        String roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .map(auth -> auth.replace("ROLE_", "")) // Remove "ROLE_" prefix for cleaner display
            .collect(Collectors.joining(", "));

        return ResponseEntity.ok(Map.of(
            "message", "Welcome to your dashboard!",
            "email", email,
            "role", roles,
            "authenticated", true
        ));
    }

    /**
     * Get role information for the current authenticated user
     * Returns email, authorities, and boolean flags for role capabilities
     *
     * @param authentication the current authentication context
     * @return RoleInfoResponse with user's role information
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/role-info")
    public ResponseEntity<RoleInfoResponse> getRoleInfo(Authentication authentication) {
        // Extract email (username in our case)
        String email = authentication.getName();

        // Extract authorities as a set of strings
        Set<String> authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

        // Determine role flags
        boolean isAdmin = authorities.contains("ROLE_ADMIN");
        boolean isUser = authorities.contains("ROLE_USER");

        // Create response with role information
        RoleInfoResponse response = new RoleInfoResponse(email, authorities, isAdmin, isUser);

        return ResponseEntity.ok(response);
    }

    /**
     * Get user profile by email - Expression-based security
     * Demonstrates fine-grained access control with SpEL (Spring Expression Language)
     *
     * Access Rules:
     * - ADMIN role can view ANY profile
     * - USER role can ONLY view their OWN profile (authentication.name == email)
     * - Trying to access another user's profile results in 403 Forbidden
     *
     * @param email the email of the user whose profile to retrieve
     * @return UserResponse with profile information
     */
    @PreAuthorize("hasRole('ADMIN') or authentication.name == #email")
    @GetMapping("/profile/{email}")
    public ResponseEntity<UserResponse> getProfile(@PathVariable String email) {
        // Find user by email
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Create response with user details
        UserResponse response = new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole().toString(),
            user.getActive(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );

        return ResponseEntity.ok(response);
    }
}
