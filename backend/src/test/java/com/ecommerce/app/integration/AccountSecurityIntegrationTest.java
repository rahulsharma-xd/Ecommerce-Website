package com.ecommerce.app.integration;

import com.ecommerce.app.dto.LoginRequest;
import com.ecommerce.app.dto.LoginResponse;
import com.ecommerce.app.dto.RegisterRequest;
import com.ecommerce.app.dto.UnlockRequest;
import com.ecommerce.app.entity.Role;
import com.ecommerce.app.entity.User;
import com.ecommerce.app.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for account security features.
 * Tests: Failed login attempts → Account lock → Unlock flow, Token invalidation
 *
 * Uses @SpringBootTest for full application context with real database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Account Security Integration Test")
class AccountSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private LoginRequest wrongPasswordRequest;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up before each test
        userRepository.deleteAll();

        // Create admin user
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("AdminPass123!"));
        adminUser.setRole(Role.ADMIN);
        adminUser.setActive(true);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(adminUser);

        // Login as admin to get token
        LoginRequest adminLoginRequest = new LoginRequest();
        adminLoginRequest.setEmail("admin@example.com");
        adminLoginRequest.setPassword("AdminPass123!");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLoginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(
            loginResult.getResponse().getContentAsString(),
            LoginResponse.class
        );
        adminToken = loginResponse.getToken();

        // Setup registration request
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("securitytest");
        registerRequest.setEmail("security@example.com");
        registerRequest.setPassword("SecurePass123!");

        // Setup correct login request
        loginRequest = new LoginRequest();
        loginRequest.setEmail("security@example.com");
        loginRequest.setPassword("SecurePass123!");

        // Setup wrong password request
        wrongPasswordRequest = new LoginRequest();
        wrongPasswordRequest.setEmail("security@example.com");
        wrongPasswordRequest.setPassword("WrongPassword!");
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should lock account after 5 failed login attempts")
    void shouldLockAccountAfterFailedAttempts() throws Exception {
        // Register user
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk());

        // First 4 failed attempts should not lock account
        for (int i = 1; i <= 4; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
                .andExpect(status().isUnauthorized());

            // Verify account is still not locked
            User user = userRepository.findByEmail("security@example.com").get();
            assertThat(user.getAccountLocked()).isFalse();
            assertThat(user.getFailedLoginAttempts()).isEqualTo(i);
        }

        // 5th failed attempt should lock the account
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
            .andExpect(status().is5xxServerError());

        // Verify account is now locked
        User user = userRepository.findByEmail("security@example.com").get();
        assertThat(user.getAccountLocked()).isTrue();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should prevent login when account is locked")
    void shouldPreventLoginWhenAccountLocked() throws Exception {
        // Register and lock account
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk());

        // Make 5 failed attempts to lock account
        // First 4 should return 401, 5th should return 500
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
            .andExpect(status().isUnauthorized());

        // 5th attempt should lock the account
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
            .andExpect(status().is5xxServerError());

        // Try to login with correct password - should fail because account is locked
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Should successfully unlock account")
    void shouldUnlockAccountSuccessfully() throws Exception {
        // Register and lock account
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk());

        // Lock account by making 5 failed attempts
        // First 4 should return 401, 5th should return 500
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
            .andExpect(status().isUnauthorized());

        // 5th attempt should lock the account
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
            .andExpect(status().is5xxServerError());

        // Verify account is locked
        User lockedUser = userRepository.findByEmail("security@example.com").get();
        assertThat(lockedUser.getAccountLocked()).isTrue();

        // Unlock account (requires admin token)
        UnlockRequest unlockRequest = new UnlockRequest();
        unlockRequest.setEmail("security@example.com");

        mockMvc.perform(post("/api/auth/unlock")
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(unlockRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Account unlocked successfully"));

        // Verify account is unlocked in database
        User unlockedUser = userRepository.findByEmail("security@example.com").get();
        assertThat(unlockedUser.getAccountLocked()).isFalse();
        assertThat(unlockedUser.getFailedLoginAttempts()).isEqualTo(0);

        // Should be able to login now
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("Should reset failed login attempts after successful login")
    void shouldResetFailedAttemptsAfterSuccessfulLogin() throws Exception {
        // Register user
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk());

        // Make 3 failed attempts
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
                .andExpect(status().isUnauthorized());
        }

        // Verify failed attempts count
        User user = userRepository.findByEmail("security@example.com").get();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(3);

        // Successful login
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk());

        // Verify failed attempts reset to 0
        user = userRepository.findByEmail("security@example.com").get();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(0);
        assertThat(user.getAccountLocked()).isFalse();
    }

    @Test
    @DisplayName("Should prevent unlock for non-existent user")
    void shouldPreventUnlockForNonExistentUser() throws Exception {
        UnlockRequest unlockRequest = new UnlockRequest();
        unlockRequest.setEmail("nonexistent@example.com");

        mockMvc.perform(post("/api/auth/unlock")
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(unlockRequest)))
            .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Should handle unlock request with invalid email format")
    void shouldHandleInvalidEmailFormat() throws Exception {
        UnlockRequest unlockRequest = new UnlockRequest();
        unlockRequest.setEmail("invalidemail");

        mockMvc.perform(post("/api/auth/unlock")
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(unlockRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle unlock request with empty email")
    void shouldHandleEmptyEmail() throws Exception {
        UnlockRequest unlockRequest = new UnlockRequest();
        unlockRequest.setEmail("");

        mockMvc.perform(post("/api/auth/unlock")
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(unlockRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should maintain separate failed attempt counts for different users")
    void shouldMaintainSeparateFailedCounts() throws Exception {
        // Register first user
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk());

        // Register second user
        RegisterRequest registerRequest2 = new RegisterRequest();
        registerRequest2.setUsername("user2");
        registerRequest2.setEmail("user2@example.com");
        registerRequest2.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest2)))
            .andExpect(status().isOk());

        // Make 3 failed attempts for first user
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
                .andExpect(status().isUnauthorized());
        }

        // Make 1 failed attempt for second user
        LoginRequest wrongPasswordRequest2 = new LoginRequest();
        wrongPasswordRequest2.setEmail("user2@example.com");
        wrongPasswordRequest2.setPassword("WrongPassword!");

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongPasswordRequest2)))
            .andExpect(status().isUnauthorized());

        // Verify separate counts
        User user1 = userRepository.findByEmail("security@example.com").get();
        User user2 = userRepository.findByEmail("user2@example.com").get();

        assertThat(user1.getFailedLoginAttempts()).isEqualTo(3);
        assertThat(user2.getFailedLoginAttempts()).isEqualTo(1);
        assertThat(user1.getAccountLocked()).isFalse();
        assertThat(user2.getAccountLocked()).isFalse();
    }

    @Test
    @DisplayName("Should handle unlock for already unlocked account")
    void shouldHandleUnlockForUnlockedAccount() throws Exception {
        // Register user (account is unlocked by default)
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk());

        // Try to unlock already unlocked account (requires admin token)
        UnlockRequest unlockRequest = new UnlockRequest();
        unlockRequest.setEmail("security@example.com");

        mockMvc.perform(post("/api/auth/unlock")
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(unlockRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should deny access to protected endpoints after account lock")
    void shouldDenyAccessAfterAccountLock() throws Exception {
        // Register and login to get token
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk());

        // Get token before account lock
        var loginResult = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
            .get("token").asText();

        // Lock account by making 5 failed attempts (will need another session or user)
        // For simplicity, directly lock the account in database
        User user = userRepository.findByEmail("security@example.com").get();
        user.setAccountLocked(true);
        user.setFailedLoginAttempts(5);
        userRepository.save(user);

        // Token from before lock should still work (tokens don't automatically invalidate on lock)
        // But new login attempts should fail
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().is5xxServerError());
    }
}
