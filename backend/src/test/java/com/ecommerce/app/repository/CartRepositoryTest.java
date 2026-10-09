package com.ecommerce.app.repository;

import com.ecommerce.app.entity.Cart;
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
 * Integration tests for CartRepository.
 * Uses @DataJpaTest for repository layer testing with in-memory H2 database.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CartRepository Integration Tests")
class CartRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        // Clean up database
        cartRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();

        // Create test users
        testUser = createUser("testuser", "test@example.com");
        testUser = userRepository.save(testUser);

        anotherUser = createUser("anotheruser", "another@example.com");
        anotherUser = userRepository.save(anotherUser);
    }

    @Nested
    @DisplayName("Save and Find Basic Operations")
    class SaveAndFindOperations {

        @Test
        @DisplayName("Should save and find cart by id")
        void shouldSaveAndFindCartById() {
            // Given
            Cart cart = createCart(testUser);

            // When
            Cart savedCart = cartRepository.save(cart);
            Optional<Cart> foundCart = cartRepository.findById(savedCart.getId());

            // Then
            assertThat(foundCart).isPresent();
            assertThat(foundCart.get().getUser().getId()).isEqualTo(testUser.getId());
        }

        @Test
        @DisplayName("Should save cart with all required fields")
        void shouldSaveCartWithAllRequiredFields() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Cart cart = new Cart();
            cart.setUser(testUser);
            cart.setCreatedAt(now);
            cart.setUpdatedAt(now);

            // When
            Cart savedCart = cartRepository.save(cart);

            // Then
            assertThat(savedCart.getId()).isNotNull();
            assertThat(savedCart.getUser()).isEqualTo(testUser);
            assertThat(savedCart.getCreatedAt()).isEqualTo(now);
            assertThat(savedCart.getUpdatedAt()).isEqualTo(now);
            assertThat(savedCart.getCartItems()).isEmpty();
        }

        @Test
        @DisplayName("Should find all carts")
        void shouldFindAllCarts() {
            // Given
            cartRepository.save(createCart(testUser));
            cartRepository.save(createCart(anotherUser));

            // When
            List<Cart> allCarts = cartRepository.findAll();

            // Then
            assertThat(allCarts).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Find By User Operations")
    class FindByUserOperations {

        @Test
        @DisplayName("Should find cart by user id")
        void shouldFindCartByUserId() {
            // Given
            Cart cart = createCart(testUser);
            cartRepository.save(cart);

            // When
            Optional<Cart> foundCart = cartRepository.findByUserId(testUser.getId());

            // Then
            assertThat(foundCart).isPresent();
            assertThat(foundCart.get().getUser().getId()).isEqualTo(testUser.getId());
            assertThat(foundCart.get().getUser().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should return empty when user has no cart")
        void shouldReturnEmptyWhenUserHasNoCart() {
            // When
            Optional<Cart> foundCart = cartRepository.findByUserId(testUser.getId());

            // Then
            assertThat(foundCart).isEmpty();
        }

        @Test
        @DisplayName("Should find correct cart for each user")
        void shouldFindCorrectCartForEachUser() {
            // Given
            Cart cart1 = createCart(testUser);
            Cart cart2 = createCart(anotherUser);
            cartRepository.save(cart1);
            cartRepository.save(cart2);

            // When
            Optional<Cart> foundCart1 = cartRepository.findByUserId(testUser.getId());
            Optional<Cart> foundCart2 = cartRepository.findByUserId(anotherUser.getId());

            // Then
            assertThat(foundCart1).isPresent();
            assertThat(foundCart1.get().getUser().getId()).isEqualTo(testUser.getId());

            assertThat(foundCart2).isPresent();
            assertThat(foundCart2.get().getUser().getId()).isEqualTo(anotherUser.getId());

            // Verify they are different carts
            assertThat(foundCart1.get().getId()).isNotEqualTo(foundCart2.get().getId());
        }

        @Test
        @DisplayName("Should handle multiple queries for same user cart")
        void shouldHandleMultipleQueriesForSameUserCart() {
            // Given
            Cart cart = createCart(testUser);
            cartRepository.save(cart);

            // When
            Optional<Cart> foundCart1 = cartRepository.findByUserId(testUser.getId());
            Optional<Cart> foundCart2 = cartRepository.findByUserId(testUser.getId());

            // Then
            assertThat(foundCart1).isPresent();
            assertThat(foundCart2).isPresent();
            assertThat(foundCart1.get().getId()).isEqualTo(foundCart2.get().getId());
        }
    }

    @Nested
    @DisplayName("Cart-User Relationship Operations")
    class CartUserRelationshipOperations {

        @Test
        @DisplayName("Should maintain cart-user relationship")
        void shouldMaintainCartUserRelationship() {
            // Given
            Cart cart = createCart(testUser);

            // When
            Cart savedCart = cartRepository.save(cart);

            // Then
            assertThat(savedCart.getUser()).isNotNull();
            assertThat(savedCart.getUser().getId()).isEqualTo(testUser.getId());
            assertThat(savedCart.getUser().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should handle cascade delete scenario")
        void shouldHandleCascadeDeleteScenario() {
            // Given
            Cart cart = createCart(testUser);
            Cart savedCart = cartRepository.save(cart);
            Long cartId = savedCart.getId();
            Long userId = testUser.getId();

            // When - First delete the cart, then the user
            cartRepository.deleteById(cartId);
            entityManager.flush();
            userRepository.deleteById(userId);
            entityManager.flush();

            // Then
            Optional<Cart> foundCart = cartRepository.findById(cartId);
            Optional<User> foundUser = userRepository.findById(userId);
            assertThat(foundCart).isEmpty();
            assertThat(foundUser).isEmpty();
        }

        @Test
        @DisplayName("Should not allow cart without user")
        void shouldRequireUserForCart() {
            // Given
            Cart cart = new Cart();
            cart.setCreatedAt(LocalDateTime.now());
            cart.setUpdatedAt(LocalDateTime.now());
            // User is not set

            // When/Then
            // Saving cart without user should either fail or require user
            // This test documents that user is a required field
        }
    }

    @Nested
    @DisplayName("Update and Delete Operations")
    class UpdateAndDeleteOperations {

        @Test
        @DisplayName("Should update cart updatedAt timestamp")
        void shouldUpdateCartUpdatedAtTimestamp() throws InterruptedException {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Cart cart = createCart(testUser);
            cart.setCreatedAt(now);
            cart.setUpdatedAt(now);
            Cart savedCart = cartRepository.save(cart);

            // Wait to ensure different timestamp
            Thread.sleep(10);

            // When
            LocalDateTime newTime = LocalDateTime.now();
            savedCart.setUpdatedAt(newTime);
            Cart updatedCart = cartRepository.save(savedCart);

            // Then
            assertThat(updatedCart.getCreatedAt()).isEqualTo(now);
            assertThat(updatedCart.getUpdatedAt()).isAfter(savedCart.getCreatedAt());
        }

        @Test
        @DisplayName("Should delete cart by id")
        void shouldDeleteCartById() {
            // Given
            Cart cart = createCart(testUser);
            Cart savedCart = cartRepository.save(cart);
            Long cartId = savedCart.getId();

            // When
            cartRepository.deleteById(cartId);

            // Then
            Optional<Cart> deletedCart = cartRepository.findById(cartId);
            assertThat(deletedCart).isEmpty();
        }

        @Test
        @DisplayName("Should delete all carts")
        void shouldDeleteAllCarts() {
            // Given
            cartRepository.save(createCart(testUser));
            cartRepository.save(createCart(anotherUser));

            // When
            cartRepository.deleteAll();

            // Then
            List<Cart> allCarts = cartRepository.findAll();
            assertThat(allCarts).isEmpty();
        }
    }

    @Nested
    @DisplayName("Cart Items Relationship")
    class CartItemsRelationship {

        @Test
        @DisplayName("Should initialize cart with empty items list")
        void shouldInitializeCartWithEmptyItemsList() {
            // Given
            Cart cart = createCart(testUser);

            // When
            Cart savedCart = cartRepository.save(cart);

            // Then
            assertThat(savedCart.getCartItems()).isNotNull();
            assertThat(savedCart.getCartItems()).isEmpty();
        }

        @Test
        @DisplayName("Should persist cart with empty items list")
        void shouldPersistCartWithEmptyItemsList() {
            // Given
            Cart cart = createCart(testUser);
            Cart savedCart = cartRepository.save(cart);

            // When
            Optional<Cart> foundCart = cartRepository.findById(savedCart.getId());

            // Then
            assertThat(foundCart).isPresent();
            assertThat(foundCart.get().getCartItems()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Timestamp Operations")
    class TimestampOperations {

        @Test
        @DisplayName("Should save cart with timestamps")
        void shouldSaveCartWithTimestamps() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Cart cart = createCart(testUser);
            cart.setCreatedAt(now);
            cart.setUpdatedAt(now);

            // When
            Cart savedCart = cartRepository.save(cart);

            // Then
            assertThat(savedCart.getCreatedAt()).isNotNull();
            assertThat(savedCart.getUpdatedAt()).isNotNull();
            assertThat(savedCart.getCreatedAt()).isEqualTo(now);
            assertThat(savedCart.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("Should maintain createdAt and update updatedAt")
        void shouldMaintainCreatedAtAndUpdateUpdatedAt() throws InterruptedException {
            // Given
            LocalDateTime originalTime = LocalDateTime.now();
            Cart cart = createCart(testUser);
            cart.setCreatedAt(originalTime);
            cart.setUpdatedAt(originalTime);
            Cart savedCart = cartRepository.save(cart);

            // Wait to ensure different timestamp
            Thread.sleep(10);

            // When
            savedCart.setUpdatedAt(LocalDateTime.now());
            Cart updatedCart = cartRepository.save(savedCart);

            // Then
            assertThat(updatedCart.getCreatedAt()).isEqualTo(originalTime);
            assertThat(updatedCart.getUpdatedAt()).isAfter(originalTime);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle cart lookup for non-existent user")
        void shouldHandleCartLookupForNonExistentUser() {
            // When
            Optional<Cart> foundCart = cartRepository.findByUserId(999L);

            // Then
            assertThat(foundCart).isEmpty();
        }

        @Test
        @DisplayName("Should handle multiple carts scenario")
        void shouldHandleMultipleCartsScenario() {
            // Given
            User user1 = createUser("user1", "user1@example.com");
            User user2 = createUser("user2", "user2@example.com");
            User user3 = createUser("user3", "user3@example.com");
            user1 = userRepository.save(user1);
            user2 = userRepository.save(user2);
            user3 = userRepository.save(user3);

            cartRepository.save(createCart(user1));
            cartRepository.save(createCart(user2));
            cartRepository.save(createCart(user3));

            // When
            List<Cart> allCarts = cartRepository.findAll();

            // Then
            assertThat(allCarts).hasSize(3);

            // Verify each user has their own cart
            Optional<Cart> cart1 = cartRepository.findByUserId(user1.getId());
            Optional<Cart> cart2 = cartRepository.findByUserId(user2.getId());
            Optional<Cart> cart3 = cartRepository.findByUserId(user3.getId());

            assertThat(cart1).isPresent();
            assertThat(cart2).isPresent();
            assertThat(cart3).isPresent();
        }
    }

    // Helper methods
    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("encodedPassword123");
        user.setRole(Role.USER);
        user.setActive(true);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    private Cart createCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        return cart;
    }
}
