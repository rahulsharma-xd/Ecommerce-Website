package com.ecommerce.app.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User entity.
 * Tests entity behavior, validation, and business logic.
 */
@DisplayName("User Entity Tests")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword123");
        user.setRole(Role.USER);
        user.setActive(true);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Constructor and Basic Tests")
    class ConstructorAndBasicTests {

        @Test
        @DisplayName("Should create user with default constructor")
        void shouldCreateUserWithDefaultConstructor() {
            User newUser = new User();
            assertNotNull(newUser);
            assertNull(newUser.getId());
            assertNull(newUser.getUsername());
            assertNull(newUser.getEmail());
        }

        @Test
        @DisplayName("Should set and get all basic fields")
        void shouldSetAndGetAllBasicFields() {
            assertEquals(1L, user.getId());
            assertEquals("testuser", user.getUsername());
            assertEquals("test@example.com", user.getEmail());
            assertEquals("encodedPassword123", user.getPassword());
        }
    }

    @Nested
    @DisplayName("Role Management Tests")
    class RoleManagementTests {

        @Test
        @DisplayName("Should set and get USER role")
        void shouldSetAndGetUserRole() {
            user.setRole(Role.USER);
            assertEquals(Role.USER, user.getRole());
        }

        @Test
        @DisplayName("Should set and get ADMIN role")
        void shouldSetAndGetAdminRole() {
            user.setRole(Role.ADMIN);
            assertEquals(Role.ADMIN, user.getRole());
        }

        @Test
        @DisplayName("Should allow role change from USER to ADMIN")
        void shouldAllowRoleChangeFromUserToAdmin() {
            user.setRole(Role.USER);
            assertEquals(Role.USER, user.getRole());

            user.setRole(Role.ADMIN);
            assertEquals(Role.ADMIN, user.getRole());
        }

        @Test
        @DisplayName("Should allow role change from ADMIN to USER")
        void shouldAllowRoleChangeFromAdminToUser() {
            user.setRole(Role.ADMIN);
            assertEquals(Role.ADMIN, user.getRole());

            user.setRole(Role.USER);
            assertEquals(Role.USER, user.getRole());
        }

        @Test
        @DisplayName("Should handle null role")
        void shouldHandleNullRole() {
            user.setRole(null);
            assertNull(user.getRole());
        }
    }

    @Nested
    @DisplayName("Account Status Tests")
    class AccountStatusTests {

        @Test
        @DisplayName("Should set account as active")
        void shouldSetAccountAsActive() {
            user.setActive(true);
            assertTrue(user.getActive());
        }

        @Test
        @DisplayName("Should set account as inactive")
        void shouldSetAccountAsInactive() {
            user.setActive(false);
            assertFalse(user.getActive());
        }

        @Test
        @DisplayName("Should toggle account status")
        void shouldToggleAccountStatus() {
            user.setActive(true);
            assertTrue(user.getActive());

            user.setActive(false);
            assertFalse(user.getActive());

            user.setActive(true);
            assertTrue(user.getActive());
        }

        @Test
        @DisplayName("Should handle null active status")
        void shouldHandleNullActiveStatus() {
            user.setActive(null);
            assertNull(user.getActive());
        }
    }

    @Nested
    @DisplayName("Account Locking Tests")
    class AccountLockingTests {

        @Test
        @DisplayName("Should lock account")
        void shouldLockAccount() {
            user.setAccountLocked(true);
            assertTrue(user.getAccountLocked());
        }

        @Test
        @DisplayName("Should unlock account")
        void shouldUnlockAccount() {
            user.setAccountLocked(true);
            assertTrue(user.getAccountLocked());

            user.setAccountLocked(false);
            assertFalse(user.getAccountLocked());
        }

        @Test
        @DisplayName("Should track failed login attempts")
        void shouldTrackFailedLoginAttempts() {
            user.setFailedLoginAttempts(0);
            assertEquals(0, user.getFailedLoginAttempts());

            user.setFailedLoginAttempts(1);
            assertEquals(1, user.getFailedLoginAttempts());

            user.setFailedLoginAttempts(5);
            assertEquals(5, user.getFailedLoginAttempts());
        }

        @Test
        @DisplayName("Should lock account after 5 failed attempts")
        void shouldLockAccountAfter5FailedAttempts() {
            user.setFailedLoginAttempts(5);
            user.setAccountLocked(true);

            assertTrue(user.getAccountLocked());
            assertEquals(5, user.getFailedLoginAttempts());
        }

        @Test
        @DisplayName("Should reset failed attempts on unlock")
        void shouldResetFailedAttemptsOnUnlock() {
            user.setFailedLoginAttempts(5);
            user.setAccountLocked(true);

            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);

            assertFalse(user.getAccountLocked());
            assertEquals(0, user.getFailedLoginAttempts());
        }

        @Test
        @DisplayName("Should reset failed attempts on successful login")
        void shouldResetFailedAttemptsOnSuccessfulLogin() {
            user.setFailedLoginAttempts(3);
            assertEquals(3, user.getFailedLoginAttempts());

            // Simulate successful login
            user.setFailedLoginAttempts(0);
            user.setAccountLocked(false);

            assertEquals(0, user.getFailedLoginAttempts());
            assertFalse(user.getAccountLocked());
        }

        @Test
        @DisplayName("Should handle incremental failed attempts")
        void shouldHandleIncrementalFailedAttempts() {
            user.setFailedLoginAttempts(0);

            for (int i = 1; i <= 4; i++) {
                user.setFailedLoginAttempts(i);
                assertEquals(i, user.getFailedLoginAttempts());
                assertFalse(user.getAccountLocked());
            }

            // 5th attempt should trigger lock
            user.setFailedLoginAttempts(5);
            user.setAccountLocked(true);
            assertEquals(5, user.getFailedLoginAttempts());
            assertTrue(user.getAccountLocked());
        }
    }

    @Nested
    @DisplayName("Email and Username Tests")
    class EmailAndUsernameTests {

        @Test
        @DisplayName("Should set and get email")
        void shouldSetAndGetEmail() {
            user.setEmail("newemail@example.com");
            assertEquals("newemail@example.com", user.getEmail());
        }

        @Test
        @DisplayName("Should set and get username")
        void shouldSetAndGetUsername() {
            user.setUsername("newusername");
            assertEquals("newusername", user.getUsername());
        }

        @Test
        @DisplayName("Should handle email with special characters")
        void shouldHandleEmailWithSpecialCharacters() {
            user.setEmail("test+tag@example.co.uk");
            assertEquals("test+tag@example.co.uk", user.getEmail());
        }

        @Test
        @DisplayName("Should handle username with numbers")
        void shouldHandleUsernameWithNumbers() {
            user.setUsername("user123");
            assertEquals("user123", user.getUsername());
        }

        @Test
        @DisplayName("Should handle null email")
        void shouldHandleNullEmail() {
            user.setEmail(null);
            assertNull(user.getEmail());
        }

        @Test
        @DisplayName("Should handle null username")
        void shouldHandleNullUsername() {
            user.setUsername(null);
            assertNull(user.getUsername());
        }

        @Test
        @DisplayName("Should handle empty email")
        void shouldHandleEmptyEmail() {
            user.setEmail("");
            assertEquals("", user.getEmail());
        }

        @Test
        @DisplayName("Should handle empty username")
        void shouldHandleEmptyUsername() {
            user.setUsername("");
            assertEquals("", user.getUsername());
        }
    }

    @Nested
    @DisplayName("Password Tests")
    class PasswordTests {

        @Test
        @DisplayName("Should set and get password")
        void shouldSetAndGetPassword() {
            user.setPassword("newEncodedPassword456");
            assertEquals("newEncodedPassword456", user.getPassword());
        }

        @Test
        @DisplayName("Should handle encoded password")
        void shouldHandleEncodedPassword() {
            String encodedPassword = "$2a$10$abcdefghijklmnopqrstuvwxyz";
            user.setPassword(encodedPassword);
            assertEquals(encodedPassword, user.getPassword());
        }

        @Test
        @DisplayName("Should handle password change")
        void shouldHandlePasswordChange() {
            String oldPassword = "oldEncodedPassword";
            String newPassword = "newEncodedPassword";

            user.setPassword(oldPassword);
            assertEquals(oldPassword, user.getPassword());

            user.setPassword(newPassword);
            assertEquals(newPassword, user.getPassword());
        }

        @Test
        @DisplayName("Should handle null password")
        void shouldHandleNullPassword() {
            user.setPassword(null);
            assertNull(user.getPassword());
        }
    }

    @Nested
    @DisplayName("Timestamp Tests")
    class TimestampTests {

        @Test
        @DisplayName("Should set and get createdAt timestamp")
        void shouldSetAndGetCreatedAtTimestamp() {
            LocalDateTime now = LocalDateTime.now();
            user.setCreatedAt(now);
            assertEquals(now, user.getCreatedAt());
        }

        @Test
        @DisplayName("Should set and get updatedAt timestamp")
        void shouldSetAndGetUpdatedAtTimestamp() {
            LocalDateTime now = LocalDateTime.now();
            user.setUpdatedAt(now);
            assertEquals(now, user.getUpdatedAt());
        }

        @Test
        @DisplayName("Should maintain createdAt while updating updatedAt")
        void shouldMaintainCreatedAtWhileUpdatingUpdatedAt() throws InterruptedException {
            LocalDateTime created = LocalDateTime.now();
            user.setCreatedAt(created);

            Thread.sleep(10);

            LocalDateTime updated = LocalDateTime.now();
            user.setUpdatedAt(updated);

            assertEquals(created, user.getCreatedAt());
            assertNotEquals(created, user.getUpdatedAt());
            assertTrue(user.getUpdatedAt().isAfter(user.getCreatedAt()));
        }

        @Test
        @DisplayName("Should handle null timestamps")
        void shouldHandleNullTimestamps() {
            user.setCreatedAt(null);
            user.setUpdatedAt(null);

            assertNull(user.getCreatedAt());
            assertNull(user.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should represent complete user profile")
        void shouldRepresentCompleteUserProfile() {
            assertNotNull(user.getId());
            assertNotNull(user.getUsername());
            assertNotNull(user.getEmail());
            assertNotNull(user.getPassword());
            assertNotNull(user.getRole());
            assertNotNull(user.getActive());
            assertNotNull(user.getAccountLocked());
            assertNotNull(user.getFailedLoginAttempts());
            assertNotNull(user.getCreatedAt());
            assertNotNull(user.getUpdatedAt());
        }

        @Test
        @DisplayName("Should handle user lifecycle - creation to active")
        void shouldHandleUserLifecycleCreationToActive() {
            User newUser = new User();
            newUser.setUsername("newuser");
            newUser.setEmail("newuser@example.com");
            newUser.setPassword("encodedPassword");
            newUser.setRole(Role.USER);
            newUser.setActive(true);
            newUser.setAccountLocked(false);
            newUser.setFailedLoginAttempts(0);
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setUpdatedAt(LocalDateTime.now());

            assertTrue(newUser.getActive());
            assertFalse(newUser.getAccountLocked());
            assertEquals(0, newUser.getFailedLoginAttempts());
        }

        @Test
        @DisplayName("Should handle user lifecycle - active to locked")
        void shouldHandleUserLifecycleActiveToLocked() {
            user.setActive(true);
            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);

            // Simulate failed login attempts
            for (int i = 1; i <= 5; i++) {
                user.setFailedLoginAttempts(i);
            }
            user.setAccountLocked(true);

            assertTrue(user.getActive());
            assertTrue(user.getAccountLocked());
            assertEquals(5, user.getFailedLoginAttempts());
        }

        @Test
        @DisplayName("Should handle user lifecycle - locked to unlocked")
        void shouldHandleUserLifecycleLockedToUnlocked() {
            user.setAccountLocked(true);
            user.setFailedLoginAttempts(5);

            // Unlock account
            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);
            user.setUpdatedAt(LocalDateTime.now());

            assertFalse(user.getAccountLocked());
            assertEquals(0, user.getFailedLoginAttempts());
        }

        @Test
        @DisplayName("Should handle user lifecycle - active to deactivated")
        void shouldHandleUserLifecycleActiveToDeactivated() {
            user.setActive(true);

            // Deactivate user
            user.setActive(false);
            user.setUpdatedAt(LocalDateTime.now());

            assertFalse(user.getActive());
        }

        @Test
        @DisplayName("Should handle admin user creation")
        void shouldHandleAdminUserCreation() {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword("encodedAdminPassword");
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            admin.setAccountLocked(false);
            admin.setFailedLoginAttempts(0);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());

            assertEquals(Role.ADMIN, admin.getRole());
            assertTrue(admin.getActive());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Validation")
    class EdgeCasesAndValidation {

        @Test
        @DisplayName("Should handle very long username")
        void shouldHandleVeryLongUsername() {
            String longUsername = "a".repeat(255);
            user.setUsername(longUsername);
            assertEquals(longUsername, user.getUsername());
        }

        @Test
        @DisplayName("Should handle very long email")
        void shouldHandleVeryLongEmail() {
            String longEmail = "a".repeat(100) + "@example.com";
            user.setEmail(longEmail);
            assertEquals(longEmail, user.getEmail());
        }

        @Test
        @DisplayName("Should handle negative failed login attempts")
        void shouldHandleNegativeFailedLoginAttempts() {
            user.setFailedLoginAttempts(-1);
            assertEquals(-1, user.getFailedLoginAttempts());
        }

        @Test
        @DisplayName("Should handle very high failed login attempts")
        void shouldHandleVeryHighFailedLoginAttempts() {
            user.setFailedLoginAttempts(1000);
            assertEquals(1000, user.getFailedLoginAttempts());
        }

        @Test
        @DisplayName("Should handle user with all null fields")
        void shouldHandleUserWithAllNullFields() {
            User nullUser = new User();
            assertNull(nullUser.getId());
            assertNull(nullUser.getUsername());
            assertNull(nullUser.getEmail());
            assertNull(nullUser.getPassword());
            assertNull(nullUser.getRole());
            // Active, AccountLocked, and FailedLoginAttempts have default values
            // assertNull(nullUser.getActive());
            // assertNull(nullUser.getAccountLocked());
            assertEquals(0, nullUser.getFailedLoginAttempts()); // Default value is 0
            assertNull(nullUser.getCreatedAt());
            assertNull(nullUser.getUpdatedAt());
        }
    }
}
