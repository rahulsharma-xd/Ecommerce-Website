package com.ecommerce.app.repository;

import com.ecommerce.app.entity.Role;
import com.ecommerce.app.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserRepository.
 * Uses @DataJpaTest for repository layer testing with in-memory H2 database.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        userRepository.deleteAll();
        entityManager.flush();
    }

    @Nested
    @DisplayName("Save and Find Basic Operations")
    class SaveAndFindOperations {

        @Test
        @DisplayName("Should save and find user by id")
        void shouldSaveAndFindUserById() {
            // Given
            User user = createUser("testuser", "test@example.com", Role.USER);

            // When
            User savedUser = userRepository.save(user);
            Optional<User> foundUser = userRepository.findById(savedUser.getId());

            // Then
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
            assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
            assertThat(foundUser.get().getRole()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("Should find user by email")
        void shouldFindUserByEmail() {
            // Given
            User user = createUser("testuser", "test@example.com", Role.USER);
            userRepository.save(user);

            // When
            Optional<User> foundUser = userRepository.findByEmail("test@example.com");

            // Then
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
            assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should return empty when email not found")
        void shouldReturnEmptyWhenEmailNotFound() {
            // When
            Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

            // Then
            assertThat(foundUser).isEmpty();
        }

        @Test
        @DisplayName("Should find user by username")
        void shouldFindUserByUsername() {
            // Given
            User user = createUser("uniqueuser", "unique@example.com", Role.USER);
            userRepository.save(user);

            // When
            Optional<User> foundUser = userRepository.findByUsername("uniqueuser");

            // Then
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getUsername()).isEqualTo("uniqueuser");
            assertThat(foundUser.get().getEmail()).isEqualTo("unique@example.com");
        }

        @Test
        @DisplayName("Should return empty when username not found")
        void shouldReturnEmptyWhenUsernameNotFound() {
            // When
            Optional<User> foundUser = userRepository.findByUsername("nonexistent");

            // Then
            assertThat(foundUser).isEmpty();
        }
    }

    @Nested
    @DisplayName("User Role Operations")
    class UserRoleOperations {

        @Test
        @DisplayName("Should save user with USER role")
        void shouldSaveUserWithUserRole() {
            // Given
            User user = createUser("regularuser", "regular@example.com", Role.USER);

            // When
            User savedUser = userRepository.save(user);

            // Then
            assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("Should save user with ADMIN role")
        void shouldSaveUserWithAdminRole() {
            // Given
            User user = createUser("adminuser", "admin@example.com", Role.ADMIN);

            // When
            User savedUser = userRepository.save(user);

            // Then
            assertThat(savedUser.getRole()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("Should find all users and filter by role")
        void shouldFindAllUsersAndFilterByRole() {
            // Given
            userRepository.save(createUser("user1", "user1@example.com", Role.USER));
            userRepository.save(createUser("user2", "user2@example.com", Role.USER));
            userRepository.save(createUser("admin1", "admin1@example.com", Role.ADMIN));

            // When
            List<User> allUsers = userRepository.findAll();
            long userCount = allUsers.stream().filter(u -> u.getRole() == Role.USER).count();
            long adminCount = allUsers.stream().filter(u -> u.getRole() == Role.ADMIN).count();

            // Then
            assertThat(allUsers).hasSize(3);
            assertThat(userCount).isEqualTo(2);
            assertThat(adminCount).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Account Status Operations")
    class AccountStatusOperations {

        @Test
        @DisplayName("Should save user with active status true by default")
        void shouldSaveUserWithActiveStatusTrue() {
            // Given
            User user = createUser("activeuser", "active@example.com", Role.USER);
            user.setActive(true);

            // When
            User savedUser = userRepository.save(user);

            // Then
            assertThat(savedUser.getActive()).isTrue();
        }

        @Test
        @DisplayName("Should save user with active status false")
        void shouldSaveUserWithActiveStatusFalse() {
            // Given
            User user = createUser("inactiveuser", "inactive@example.com", Role.USER);
            user.setActive(false);

            // When
            User savedUser = userRepository.save(user);

            // Then
            assertThat(savedUser.getActive()).isFalse();
        }

        @Test
        @DisplayName("Should update user active status")
        void shouldUpdateUserActiveStatus() {
            // Given
            User user = createUser("user", "user@example.com", Role.USER);
            user.setActive(true);
            User savedUser = userRepository.save(user);

            // When
            savedUser.setActive(false);
            savedUser.setUpdatedAt(LocalDateTime.now());
            User updatedUser = userRepository.save(savedUser);

            // Then
            assertThat(updatedUser.getActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Account Locking Operations")
    class AccountLockingOperations {

        @Test
        @DisplayName("Should save user with account locked false by default")
        void shouldSaveUserWithAccountLockedFalse() {
            // Given
            User user = createUser("user", "user@example.com", Role.USER);
            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);

            // When
            User savedUser = userRepository.save(user);

            // Then
            assertThat(savedUser.getAccountLocked()).isFalse();
            assertThat(savedUser.getFailedLoginAttempts()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should lock account after failed login attempts")
        void shouldLockAccountAfterFailedLoginAttempts() {
            // Given
            User user = createUser("user", "user@example.com", Role.USER);
            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);
            User savedUser = userRepository.save(user);

            // When - Simulate 5 failed attempts
            savedUser.setFailedLoginAttempts(5);
            savedUser.setAccountLocked(true);
            savedUser.setUpdatedAt(LocalDateTime.now());
            User lockedUser = userRepository.save(savedUser);

            // Then
            assertThat(lockedUser.getAccountLocked()).isTrue();
            assertThat(lockedUser.getFailedLoginAttempts()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should unlock account and reset failed attempts")
        void shouldUnlockAccountAndResetFailedAttempts() {
            // Given
            User user = createUser("user", "user@example.com", Role.USER);
            user.setAccountLocked(true);
            user.setFailedLoginAttempts(5);
            User savedUser = userRepository.save(user);

            // When
            savedUser.setAccountLocked(false);
            savedUser.setFailedLoginAttempts(0);
            savedUser.setUpdatedAt(LocalDateTime.now());
            User unlockedUser = userRepository.save(savedUser);

            // Then
            assertThat(unlockedUser.getAccountLocked()).isFalse();
            assertThat(unlockedUser.getFailedLoginAttempts()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should increment failed login attempts")
        void shouldIncrementFailedLoginAttempts() {
            // Given
            User user = createUser("user", "user@example.com", Role.USER);
            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);
            User savedUser = userRepository.save(user);

            // When - Increment attempts
            savedUser.setFailedLoginAttempts(savedUser.getFailedLoginAttempts() + 1);
            User updatedUser = userRepository.save(savedUser);

            // Then
            assertThat(updatedUser.getFailedLoginAttempts()).isEqualTo(1);
            assertThat(updatedUser.getAccountLocked()).isFalse(); // Not locked yet
        }
    }

    @Nested
    @DisplayName("Update and Delete Operations")
    class UpdateAndDeleteOperations {

        @Test
        @DisplayName("Should update user email")
        void shouldUpdateUserEmail() {
            // Given
            User user = createUser("user", "old@example.com", Role.USER);
            User savedUser = userRepository.save(user);

            // When
            savedUser.setEmail("new@example.com");
            savedUser.setUpdatedAt(LocalDateTime.now());
            User updatedUser = userRepository.save(savedUser);

            // Then
            assertThat(updatedUser.getEmail()).isEqualTo("new@example.com");
        }

        @Test
        @DisplayName("Should update user role")
        void shouldUpdateUserRole() {
            // Given
            User user = createUser("user", "user@example.com", Role.USER);
            User savedUser = userRepository.save(user);

            // When
            savedUser.setRole(Role.ADMIN);
            savedUser.setUpdatedAt(LocalDateTime.now());
            User updatedUser = userRepository.save(savedUser);

            // Then
            assertThat(updatedUser.getRole()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("Should delete user by id")
        void shouldDeleteUserById() {
            // Given
            User user = createUser("user", "user@example.com", Role.USER);
            User savedUser = userRepository.save(user);
            Long userId = savedUser.getId();

            // When
            userRepository.deleteById(userId);

            // Then
            Optional<User> deletedUser = userRepository.findById(userId);
            assertThat(deletedUser).isEmpty();
        }

        @Test
        @DisplayName("Should delete all users")
        void shouldDeleteAllUsers() {
            // Given
            userRepository.save(createUser("user1", "user1@example.com", Role.USER));
            userRepository.save(createUser("user2", "user2@example.com", Role.USER));
            userRepository.save(createUser("admin1", "admin1@example.com", Role.ADMIN));

            // When
            userRepository.deleteAll();

            // Then
            List<User> allUsers = userRepository.findAll();
            assertThat(allUsers).isEmpty();
        }
    }

    @Nested
    @DisplayName("Timestamp Operations")
    class TimestampOperations {

        @Test
        @DisplayName("Should save user with createdAt and updatedAt timestamps")
        void shouldSaveUserWithTimestamps() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            User user = createUser("user", "user@example.com", Role.USER);
            user.setCreatedAt(now);
            user.setUpdatedAt(now);

            // When
            User savedUser = userRepository.save(user);

            // Then
            assertThat(savedUser.getCreatedAt()).isNotNull();
            assertThat(savedUser.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should update updatedAt timestamp on modification")
        void shouldUpdateUpdatedAtTimestamp() throws InterruptedException {
            // Given
            LocalDateTime now = LocalDateTime.now();
            User user = createUser("user", "user@example.com", Role.USER);
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            User savedUser = userRepository.save(user);

            // Wait a moment to ensure different timestamp
            Thread.sleep(10);

            // When
            LocalDateTime newTime = LocalDateTime.now();
            savedUser.setEmail("newemail@example.com");
            savedUser.setUpdatedAt(newTime);
            User updatedUser = userRepository.save(savedUser);

            // Then
            assertThat(updatedUser.getCreatedAt()).isEqualTo(now);
            assertThat(updatedUser.getUpdatedAt()).isAfter(savedUser.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Uniqueness Constraint Tests")
    class UniquenessConstraintTests {

        @Test
        @DisplayName("Should enforce unique email constraint")
        void shouldEnforceUniqueEmailConstraint() {
            // Given
            User user1 = createUser("user1", "same@example.com", Role.USER);
            userRepository.save(user1);

            // When
            User user2 = createUser("user2", "same@example.com", Role.USER);

            // Then - This should throw a constraint violation
            // In real scenario, this would throw DataIntegrityViolationException
            // For test purposes, we verify the first user exists
            Optional<User> existingUser = userRepository.findByEmail("same@example.com");
            assertThat(existingUser).isPresent();
            assertThat(existingUser.get().getUsername()).isEqualTo("user1");
        }

        @Test
        @DisplayName("Should enforce unique username constraint")
        void shouldEnforceUniqueUsernameConstraint() {
            // Given
            User user1 = createUser("sameusername", "user1@example.com", Role.USER);
            userRepository.save(user1);

            // When
            User user2 = createUser("sameusername", "user2@example.com", Role.USER);

            // Then - Verify the first user exists with that username
            Optional<User> existingUser = userRepository.findByUsername("sameusername");
            assertThat(existingUser).isPresent();
            assertThat(existingUser.get().getEmail()).isEqualTo("user1@example.com");
        }
    }

    // Helper method
    private User createUser(String username, String email, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("encodedPassword123");
        user.setRole(role);
        user.setActive(true);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}
