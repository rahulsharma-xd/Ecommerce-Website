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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * Tests authentication business logic with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Setup valid register request
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setUsername("testuser");
        validRegisterRequest.setEmail("test@example.com");
        validRegisterRequest.setPassword("password123");

        // Setup valid login request
        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("password123");

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.USER);
        testUser.setAccountLocked(false);
        testUser.setFailedLoginAttempts(0);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("User Registration Tests")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register user successfully with valid data")
        void shouldRegisterUserSuccessfully() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            RegisterResponse response = authService.registerUser(validRegisterRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getUsername()).isEqualTo("testuser");
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getMessage()).isEqualTo("User registered successfully");

            verify(userRepository).findByEmail("test@example.com");
            verify(userRepository).findByUsername("testuser");
            verify(passwordEncoder).encode("password123");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

            // When/Then
            assertThatThrownBy(() -> authService.registerUser(validRegisterRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Email already registered");

            verify(userRepository).findByEmail("test@example.com");
            verify(userRepository, never()).findByUsername(anyString());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when username already exists")
        void shouldThrowExceptionWhenUsernameExists() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

            // When/Then
            assertThatThrownBy(() -> authService.registerUser(validRegisterRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Username already taken");

            verify(userRepository).findByEmail("test@example.com");
            verify(userRepository).findByUsername("testuser");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when username is null")
        void shouldThrowExceptionWhenUsernameIsNull() {
            // Given
            validRegisterRequest.setUsername(null);

            // When/Then
            assertThatThrownBy(() -> authService.registerUser(validRegisterRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Username is required");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when username is empty")
        void shouldThrowExceptionWhenUsernameIsEmpty() {
            // Given
            validRegisterRequest.setUsername("   ");

            // When/Then
            assertThatThrownBy(() -> authService.registerUser(validRegisterRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Username is required");
        }

        @Test
        @DisplayName("Should throw exception when email is null")
        void shouldThrowExceptionWhenEmailIsNull() {
            // Given
            validRegisterRequest.setEmail(null);

            // When/Then
            assertThatThrownBy(() -> authService.registerUser(validRegisterRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Email is required");
        }

        @Test
        @DisplayName("Should throw exception when email is empty")
        void shouldThrowExceptionWhenEmailIsEmpty() {
            // Given
            validRegisterRequest.setEmail("   ");

            // When/Then
            assertThatThrownBy(() -> authService.registerUser(validRegisterRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Email is required");
        }

        @Test
        @DisplayName("Should throw exception when password is null")
        void shouldThrowExceptionWhenPasswordIsNull() {
            // Given
            validRegisterRequest.setPassword(null);

            // When/Then
            assertThatThrownBy(() -> authService.registerUser(validRegisterRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Password is required");
        }

        @Test
        @DisplayName("Should throw exception when password is empty")
        void shouldThrowExceptionWhenPasswordIsEmpty() {
            // Given
            validRegisterRequest.setPassword("");

            // When/Then
            assertThatThrownBy(() -> authService.registerUser(validRegisterRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Password is required");
        }
    }

    @Nested
    @DisplayName("Admin Registration Tests")
    class AdminRegistrationTests {

        @Test
        @DisplayName("Should register admin successfully with valid data")
        void shouldRegisterAdminSuccessfully() {
            // Given
            User adminUser = new User();
            adminUser.setId(2L);
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword("encodedPassword");
            adminUser.setRole(Role.ADMIN);

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(adminUser);

            // When
            RegisterResponse response = authService.registerAdmin(validRegisterRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(2L);
            assertThat(response.getMessage()).isEqualTo("Admin registered successfully");

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when admin email already exists")
        void shouldThrowExceptionWhenAdminEmailExists() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

            // When/Then
            assertThatThrownBy(() -> authService.registerAdmin(validRegisterRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Email already registered");
        }

        @Test
        @DisplayName("Should validate admin registration input fields")
        void shouldValidateAdminRegistrationInputs() {
            // Given
            validRegisterRequest.setUsername(null);

            // When/Then
            assertThatThrownBy(() -> authService.registerAdmin(validRegisterRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Username is required");
        }
    }

    @Nested
    @DisplayName("User Login Tests")
    class UserLoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfully() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(jwtUtil.generateToken(anyString())).thenReturn("jwt-token-123");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            LoginResponse response = authService.loginUser(validLoginRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getUsername()).isEqualTo("testuser");
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getRole()).isEqualTo("USER");
            assertThat(response.getMessage()).isEqualTo("Login successful");
            assertThat(response.getToken()).isEqualTo("jwt-token-123");

            verify(userRepository).findByEmail("test@example.com");
            verify(passwordEncoder).matches("password123", "encodedPassword");
            verify(jwtUtil).generateToken("test@example.com");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should reset failed attempts on successful login")
        void shouldResetFailedAttemptsOnSuccessfulLogin() {
            // Given
            testUser.setFailedLoginAttempts(3);
            testUser.setAccountLocked(false);

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(jwtUtil.generateToken(anyString())).thenReturn("jwt-token-123");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            authService.loginUser(validLoginRequest);

            // Then
            verify(userRepository).save(argThat(user ->
                user.getFailedLoginAttempts() == 0 && !user.getAccountLocked()
            ));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authService.loginUser(validLoginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

            verify(userRepository).findByEmail("test@example.com");
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw exception when password is incorrect")
        void shouldThrowExceptionWhenPasswordIncorrect() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When/Then
            assertThatThrownBy(() -> authService.loginUser(validLoginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");

            verify(passwordEncoder).matches("password123", "encodedPassword");
        }

        @Test
        @DisplayName("Should increment failed attempts on wrong password")
        void shouldIncrementFailedAttemptsOnWrongPassword() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When/Then
            assertThatThrownBy(() -> authService.loginUser(validLoginRequest))
                .isInstanceOf(InvalidCredentialsException.class);

            verify(userRepository).save(argThat(user -> user.getFailedLoginAttempts() == 1));
        }

        @Test
        @DisplayName("Should lock account after 5 failed login attempts")
        void shouldLockAccountAfter5FailedAttempts() {
            // Given
            testUser.setFailedLoginAttempts(4);
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When/Then
            assertThatThrownBy(() -> authService.loginUser(validLoginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Account locked !");

            verify(userRepository, times(2)).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when account is already locked")
        void shouldThrowExceptionWhenAccountLocked() {
            // Given
            testUser.setAccountLocked(true);
            testUser.setFailedLoginAttempts(5);
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            // Password matcher not needed - lock check happens before password check

            // When/Then
            assertThatThrownBy(() -> authService.loginUser(validLoginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Account is locked due to too many failed login attempts");
        }

        @Test
        @DisplayName("Should throw exception when email is null")
        void shouldThrowExceptionWhenEmailIsNull() {
            // Given
            validLoginRequest.setEmail(null);

            // When/Then
            assertThatThrownBy(() -> authService.loginUser(validLoginRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Email is required");
        }

        @Test
        @DisplayName("Should throw exception when email is empty")
        void shouldThrowExceptionWhenEmailIsEmpty() {
            // Given
            validLoginRequest.setEmail("   ");

            // When/Then
            assertThatThrownBy(() -> authService.loginUser(validLoginRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Email is required");
        }

        @Test
        @DisplayName("Should throw exception when password is null")
        void shouldThrowExceptionWhenPasswordIsNull() {
            // Given
            validLoginRequest.setPassword(null);

            // When/Then
            assertThatThrownBy(() -> authService.loginUser(validLoginRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Password is required");
        }

        @Test
        @DisplayName("Should throw exception when password is empty")
        void shouldThrowExceptionWhenPasswordIsEmpty() {
            // Given
            validLoginRequest.setPassword("");

            // When/Then
            assertThatThrownBy(() -> authService.loginUser(validLoginRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Password is required");
        }
    }

    @Nested
    @DisplayName("Account Unlock Tests")
    class AccountUnlockTests {

        @Test
        @DisplayName("Should unlock account successfully")
        void shouldUnlockAccountSuccessfully() {
            // Given
            testUser.setAccountLocked(true);
            testUser.setFailedLoginAttempts(5);

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            UnlockResponse response = authService.unlockAccount("test@example.com");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(1L);
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getMessage()).isEqualTo("Account unlocked successfully");
            assertThat(response.getSuccess()).isTrue();

            verify(userRepository).findByEmail("test@example.com");
            verify(userRepository).save(argThat(user ->
                !user.getAccountLocked() && user.getFailedLoginAttempts() == 0
            ));
        }

        @Test
        @DisplayName("Should reset failed login attempts when unlocking")
        void shouldResetFailedLoginAttemptsWhenUnlocking() {
            // Given
            testUser.setAccountLocked(true);
            testUser.setFailedLoginAttempts(10);

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            authService.unlockAccount("test@example.com");

            // Then
            verify(userRepository).save(argThat(user -> user.getFailedLoginAttempts() == 0));
        }

        @Test
        @DisplayName("Should throw exception when user not found for unlock")
        void shouldThrowExceptionWhenUserNotFoundForUnlock() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authService.unlockAccount("nonexistent@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

            verify(userRepository).findByEmail("nonexistent@example.com");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should unlock account even if already unlocked")
        void shouldUnlockAccountEvenIfAlreadyUnlocked() {
            // Given
            testUser.setAccountLocked(false);
            testUser.setFailedLoginAttempts(0);

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            UnlockResponse response = authService.unlockAccount("test@example.com");

            // Then
            assertThat(response.getSuccess()).isTrue();
            verify(userRepository).save(any(User.class));
        }
    }
}
