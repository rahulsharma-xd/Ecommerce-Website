package com.ecommerce.app.controller;

import com.ecommerce.app.entity.User;
import com.ecommerce.app.entity.Role;
import com.ecommerce.app.repository.UserRepository;
import com.ecommerce.app.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello from E-commerce API!");
        response.put("status", "success");
        response.put("port", "8082");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test/create-user")
    public String createTestUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return "User created with ID: " + user.getId();
    }

    @GetMapping("/test/find-user")
    public String findUser() {
        Optional<User> userOptional = userRepository.findByEmail("test@example.com");

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return "Found user: " + user.getUsername() + " (ID: " + user.getId() + ")";
        } else {
            return "User not found";
        }
    }

    @GetMapping("/test/count-users")
    public String countUsers() {
        long count = userRepository.count();
        return "Total users in database: " + count;
    }

    @GetMapping("/test/generate-jwt")
    public String generateTestJWT() {
        String email = "test@example.com";
        String token = jwtUtil.generateToken(email);
        return "Generated JWT Token:\n" + token + "\n\nExtracted Email: " + jwtUtil.extractEmail(token);
    }
}
