package com.ecommerce.app.repository;

import com.ecommerce.app.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for CartItemRepository.
 * Uses @DataJpaTest for repository layer testing with in-memory H2 database.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CartItemRepository Integration Tests")
class CartItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Cart testCart;
    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        // Clean up database
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();

        // Create test user
        testUser = createUser("testuser", "test@example.com");
        testUser = userRepository.save(testUser);

        // Create test cart
        testCart = createCart(testUser);
        testCart = cartRepository.save(testCart);

        // Create test products
        testProduct1 = createProduct("Product 1", new BigDecimal("99.99"), 100);
        testProduct1 = productRepository.save(testProduct1);

        testProduct2 = createProduct("Product 2", new BigDecimal("49.99"), 50);
        testProduct2 = productRepository.save(testProduct2);
    }

    @Nested
    @DisplayName("Save and Find Basic Operations")
    class SaveAndFindOperations {

        @Test
        @DisplayName("Should save and find cart item by id")
        void shouldSaveAndFindCartItemById() {
            // Given
            CartItem cartItem = createCartItem(testCart, testProduct1, 2, new BigDecimal("99.99"));

            // When
            CartItem savedItem = cartItemRepository.save(cartItem);
            Optional<CartItem> foundItem = cartItemRepository.findById(savedItem.getId());

            // Then
            assertThat(foundItem).isPresent();
            assertThat(foundItem.get().getQuantity()).isEqualTo(2);
            assertThat(foundItem.get().getPriceAtAddition()).isEqualByComparingTo(new BigDecimal("99.99"));
        }

        @Test
        @DisplayName("Should save cart item with all required fields")
        void shouldSaveCartItemWithAllRequiredFields() {
            // Given
            CartItem cartItem = new CartItem();
            cartItem.setCart(testCart);
            cartItem.setProduct(testProduct1);
            cartItem.setQuantity(3);
            cartItem.setPriceAtAddition(new BigDecimal("99.99"));
            cartItem.setCreatedAt(LocalDateTime.now());
            cartItem.setUpdatedAt(LocalDateTime.now());

            // When
            CartItem savedItem = cartItemRepository.save(cartItem);

            // Then
            assertThat(savedItem.getId()).isNotNull();
            assertThat(savedItem.getCart().getId()).isEqualTo(testCart.getId());
            assertThat(savedItem.getProduct().getId()).isEqualTo(testProduct1.getId());
            assertThat(savedItem.getQuantity()).isEqualTo(3);
            assertThat(savedItem.getPriceAtAddition()).isEqualByComparingTo(new BigDecimal("99.99"));
        }

        @Test
        @DisplayName("Should find all cart items")
        void shouldFindAllCartItems() {
            // Given
            cartItemRepository.save(createCartItem(testCart, testProduct1, 2, new BigDecimal("99.99")));
            cartItemRepository.save(createCartItem(testCart, testProduct2, 1, new BigDecimal("49.99")));

            // When
            List<CartItem> allItems = cartItemRepository.findAll();

            // Then
            assertThat(allItems).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Find By Cart and Product")
    class FindByCartAndProduct {

        @Test
        @DisplayName("Should find cart item by cart id and product id")
        void shouldFindCartItemByCartIdAndProductId() {
            // Given
            CartItem cartItem = createCartItem(testCart, testProduct1, 2, new BigDecimal("99.99"));
            cartItemRepository.save(cartItem);

            // When
            Optional<CartItem> foundItem = cartItemRepository.findByCartIdAndProductId(
                testCart.getId(),
                testProduct1.getId()
            );

            // Then
            assertThat(foundItem).isPresent();
            assertThat(foundItem.get().getCart().getId()).isEqualTo(testCart.getId());
            assertThat(foundItem.get().getProduct().getId()).isEqualTo(testProduct1.getId());
            assertThat(foundItem.get().getQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return empty when cart-product combination not found")
        void shouldReturnEmptyWhenCartProductCombinationNotFound() {
            // When
            Optional<CartItem> foundItem = cartItemRepository.findByCartIdAndProductId(
                testCart.getId(),
                testProduct1.getId()
            );

            // Then
            assertThat(foundItem).isEmpty();
        }

        @Test
        @DisplayName("Should find correct item for multiple items in cart")
        void shouldFindCorrectItemForMultipleItemsInCart() {
            // Given
            CartItem item1 = createCartItem(testCart, testProduct1, 2, new BigDecimal("99.99"));
            CartItem item2 = createCartItem(testCart, testProduct2, 3, new BigDecimal("49.99"));
            cartItemRepository.save(item1);
            cartItemRepository.save(item2);

            // When
            Optional<CartItem> foundItem1 = cartItemRepository.findByCartIdAndProductId(
                testCart.getId(), testProduct1.getId());
            Optional<CartItem> foundItem2 = cartItemRepository.findByCartIdAndProductId(
                testCart.getId(), testProduct2.getId());

            // Then
            assertThat(foundItem1).isPresent();
            assertThat(foundItem1.get().getProduct().getId()).isEqualTo(testProduct1.getId());
            assertThat(foundItem1.get().getQuantity()).isEqualTo(2);

            assertThat(foundItem2).isPresent();
            assertThat(foundItem2.get().getProduct().getId()).isEqualTo(testProduct2.getId());
            assertThat(foundItem2.get().getQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should not find item from different cart")
        void shouldNotFindItemFromDifferentCart() {
            // Given
            User anotherUser = createUser("another", "another@example.com");
            anotherUser = userRepository.save(anotherUser);
            Cart anotherCart = createCart(anotherUser);
            anotherCart = cartRepository.save(anotherCart);

            CartItem item = createCartItem(testCart, testProduct1, 2, new BigDecimal("99.99"));
            cartItemRepository.save(item);

            // When
            Optional<CartItem> foundItem = cartItemRepository.findByCartIdAndProductId(
                anotherCart.getId(),
                testProduct1.getId()
            );

            // Then
            assertThat(foundItem).isEmpty();
        }
    }

    @Nested
    @DisplayName("Cart Item Relationships")
    class CartItemRelationships {

        @Test
        @DisplayName("Should maintain cart-item relationship")
        void shouldMaintainCartItemRelationship() {
            // Given
            CartItem cartItem = createCartItem(testCart, testProduct1, 2, new BigDecimal("99.99"));

            // When
            CartItem savedItem = cartItemRepository.save(cartItem);

            // Then
            assertThat(savedItem.getCart()).isNotNull();
            assertThat(savedItem.getCart().getId()).isEqualTo(testCart.getId());
            assertThat(savedItem.getCart().getUser().getId()).isEqualTo(testUser.getId());
        }

        @Test
        @DisplayName("Should maintain product-item relationship")
        void shouldMaintainProductItemRelationship() {
            // Given
            CartItem cartItem = createCartItem(testCart, testProduct1, 2, new BigDecimal("99.99"));

            // When
            CartItem savedItem = cartItemRepository.save(cartItem);

            // Then
            assertThat(savedItem.getProduct()).isNotNull();
            assertThat(savedItem.getProduct().getId()).isEqualTo(testProduct1.getId());
            assertThat(savedItem.getProduct().getName()).isEqualTo("Product 1");
            assertThat(savedItem.getProduct().getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
        }

        @Test
        @DisplayName("Should store price at addition time")
        void shouldStorePriceAtAdditionTime() {
            // Given
            CartItem cartItem = createCartItem(testCart, testProduct1, 2, new BigDecimal("99.99"));

            // When
            CartItem savedItem = cartItemRepository.save(cartItem);

            // Update product price (should not affect cart item price)
            testProduct1.setPrice(new BigDecimal("89.99"));
            productRepository.save(testProduct1);

            // Then
            CartItem foundItem = cartItemRepository.findById(savedItem.getId()).get();
            assertThat(foundItem.getPriceAtAddition()).isEqualByComparingTo(new BigDecimal("99.99"));
            assertThat(foundItem.getProduct().getPrice()).isEqualByComparingTo(new BigDecimal("89.99"));
        }
    }

    @Nested
    @DisplayName("Quantity Operations")
    class QuantityOperations {

        @Test
        @DisplayName("Should save cart item with quantity")
        void shouldSaveCartItemWithQuantity() {
            // Given
            CartItem cartItem = createCartItem(testCart, testProduct1, 5, new BigDecimal("99.99"));

            // When
            CartItem savedItem = cartItemRepository.save(cartItem);

            // Then
            assertThat(savedItem.getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should update cart item quantity")
        void shouldUpdateCartItemQuantity() {
            // Given
            CartItem cartItem = createCartItem(testCart, testProduct1, 2, new BigDecimal("99.99"));
            CartItem savedItem = cartItemRepository.save(cartItem);

            // When
            savedItem.setQuantity(5);
            savedItem.setUpdatedAt(LocalDateTime.now());
            CartItem updatedItem = cartItemRepository.save(savedItem);

            // Then
            assertThat(updatedItem.getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should handle quantity of 1")
        void shouldHandleQuantityOfOne() {
            // Given
            CartItem cartItem = createCartItem(testCart, testProduct1, 1, new BigDecimal("99.99"));

            // When
            CartItem savedItem = cartItemRepository.save(cartItem);

            // Then
            assertThat(savedItem.getQuantity()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle large quantities")
        void shouldHandleLargeQuantities() {
            // Given
            CartItem cartItem = createCartItem(testCart, testProduct1, 999, new BigDecimal("99.99"));

            // When
            CartItem savedItem = cartItemRepository.save(cartItem);

            // Then
            assertThat(savedItem.getQuantity()).isEqualTo(999);
        }
    }

    @Nested
    @DisplayName("Update and Delete Operations")
    class UpdateAndDeleteOperations {

        @Test
        @DisplayName("Should update cart item")
        void shouldUpdateCartItem() {
            // Given
            CartItem cartItem = createCartItem(testCart, testProduct1, 2, new BigDecimal("99.99"));
            CartItem savedItem = cartItemRepository.save(cartItem);

            // When
            savedItem.setQuantity(4);
            savedItem.setUpdatedAt(LocalDateTime.now());
            CartItem updatedItem = cartItemRepository.save(savedItem);

            // Then
            assertThat(updatedItem.getQuantity()).isEqualTo(4);
            assertThat(updatedItem.getPriceAtAddition()).isEqualByComparingTo(new BigDecimal("99.99"));
        }

        @Test
        @DisplayName("Should delete cart item by id")
        void shouldDeleteCartItemById() {
            // Given
            CartItem cartItem = createCartItem(testCart, testProduct1, 2, new BigDecimal("99.99"));
            CartItem savedItem = cartItemRepository.save(cartItem);
            Long itemId = savedItem.getId();

            // When
            cartItemRepository.deleteById(itemId);

            // Then
            Optional<CartItem> deletedItem = cartItemRepository.findById(itemId);
            assertThat(deletedItem).isEmpty();
        }

        @Test
        @DisplayName("Should delete all cart items")
        void shouldDeleteAllCartItems() {
            // Given
            cartItemRepository.save(createCartItem(testCart, testProduct1, 2, new BigDecimal("99.99")));
            cartItemRepository.save(createCartItem(testCart, testProduct2, 1, new BigDecimal("49.99")));

            // When
            cartItemRepository.deleteAll();

            // Then
            List<CartItem> allItems = cartItemRepository.findAll();
            assertThat(allItems).isEmpty();
        }
    }

    @Nested
    @DisplayName("Timestamp Operations")
    class TimestampOperations {

        @Test
        @DisplayName("Should save cart item with timestamps")
        void shouldSaveCartItemWithTimestamps() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CartItem cartItem = createCartItem(testCart, testProduct1, 2, new BigDecimal("99.99"));
            cartItem.setCreatedAt(now);
            cartItem.setUpdatedAt(now);

            // When
            CartItem savedItem = cartItemRepository.save(cartItem);

            // Then
            assertThat(savedItem.getCreatedAt()).isNotNull();
            assertThat(savedItem.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should update updatedAt timestamp on modification")
        void shouldUpdateUpdatedAtTimestamp() throws InterruptedException {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CartItem cartItem = createCartItem(testCart, testProduct1, 2, new BigDecimal("99.99"));
            cartItem.setCreatedAt(now);
            cartItem.setUpdatedAt(now);
            CartItem savedItem = cartItemRepository.save(cartItem);

            // Wait to ensure different timestamp
            Thread.sleep(10);

            // When
            LocalDateTime newTime = LocalDateTime.now();
            savedItem.setQuantity(3);
            savedItem.setUpdatedAt(newTime);
            CartItem updatedItem = cartItemRepository.save(savedItem);

            // Then
            assertThat(updatedItem.getCreatedAt()).isEqualTo(now);
            assertThat(updatedItem.getUpdatedAt()).isAfter(savedItem.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Complex Queries and Edge Cases")
    class ComplexQueriesAndEdgeCases {

        @Test
        @DisplayName("Should handle multiple items in same cart")
        void shouldHandleMultipleItemsInSameCart() {
            // Given
            cartItemRepository.save(createCartItem(testCart, testProduct1, 2, new BigDecimal("99.99")));
            cartItemRepository.save(createCartItem(testCart, testProduct2, 3, new BigDecimal("49.99")));

            // When
            List<CartItem> allItems = cartItemRepository.findAll();
            List<CartItem> cartItems = allItems.stream()
                .filter(item -> item.getCart().getId().equals(testCart.getId()))
                .toList();

            // Then
            assertThat(cartItems).hasSize(2);
        }

        @Test
        @DisplayName("Should handle same product in different carts")
        void shouldHandleSameProductInDifferentCarts() {
            // Given
            User anotherUser = createUser("another", "another@example.com");
            anotherUser = userRepository.save(anotherUser);
            Cart anotherCart = createCart(anotherUser);
            anotherCart = cartRepository.save(anotherCart);

            cartItemRepository.save(createCartItem(testCart, testProduct1, 2, new BigDecimal("99.99")));
            cartItemRepository.save(createCartItem(anotherCart, testProduct1, 3, new BigDecimal("99.99")));

            // When
            Optional<CartItem> item1 = cartItemRepository.findByCartIdAndProductId(
                testCart.getId(), testProduct1.getId());
            Optional<CartItem> item2 = cartItemRepository.findByCartIdAndProductId(
                anotherCart.getId(), testProduct1.getId());

            // Then
            assertThat(item1).isPresent();
            assertThat(item2).isPresent();
            assertThat(item1.get().getId()).isNotEqualTo(item2.get().getId());
            assertThat(item1.get().getQuantity()).isEqualTo(2);
            assertThat(item2.get().getQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should handle cart item with non-existent IDs gracefully")
        void shouldHandleCartItemWithNonExistentIds() {
            // When
            Optional<CartItem> foundItem = cartItemRepository.findByCartIdAndProductId(999L, 999L);

            // Then
            assertThat(foundItem).isEmpty();
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

    private Product createProduct(String name, BigDecimal price, Integer stockQuantity) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("Description for " + name);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setCategory("Test Category");
        product.setBrand("Test Brand");
        product.setImageUrl("http://example.com/image.jpg");
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    private CartItem createCartItem(Cart cart, Product product, Integer quantity, BigDecimal priceAtAddition) {
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setPriceAtAddition(priceAtAddition);
        cartItem.setCreatedAt(LocalDateTime.now());
        cartItem.setUpdatedAt(LocalDateTime.now());
        return cartItem;
    }
}
