package com.ecommerce.app.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Cart entity.
 * Tests entity behavior, relationships, and business logic.
 */
@DisplayName("Cart Entity Tests")
class CartTest {

    private Cart cart;
    private User user;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        // Setup user
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        // Setup cart
        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());

        // Setup product for cart items
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("99.99"));
        product.setStockQuantity(100);

        // Setup cart item
        cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setPriceAtAddition(new BigDecimal("99.99"));
        cartItem.setCreatedAt(LocalDateTime.now());
        cartItem.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Constructor and Basic Tests")
    class ConstructorAndBasicTests {

        @Test
        @DisplayName("Should create cart with default constructor")
        void shouldCreateCartWithDefaultConstructor() {
            Cart newCart = new Cart();
            assertNotNull(newCart);
            assertNull(newCart.getId());
            assertNull(newCart.getUser());
            assertNotNull(newCart.getCartItems());
            assertTrue(newCart.getCartItems().isEmpty());
        }

        @Test
        @DisplayName("Should set and get all basic fields")
        void shouldSetAndGetAllBasicFields() {
            assertEquals(1L, cart.getId());
            assertEquals(user, cart.getUser());
            assertNotNull(cart.getCreatedAt());
            assertNotNull(cart.getUpdatedAt());
        }

        @Test
        @DisplayName("Should initialize cart items as empty list")
        void shouldInitializeCartItemsAsEmptyList() {
            Cart newCart = new Cart();
            assertNotNull(newCart.getCartItems());
            assertTrue(newCart.getCartItems().isEmpty());
            assertEquals(0, newCart.getCartItems().size());
        }
    }

    @Nested
    @DisplayName("Cart-User Relationship Tests")
    class CartUserRelationshipTests {

        @Test
        @DisplayName("Should set and get user")
        void shouldSetAndGetUser() {
            User newUser = new User();
            newUser.setId(2L);
            newUser.setUsername("newuser");

            cart.setUser(newUser);

            assertEquals(newUser, cart.getUser());
            assertEquals(2L, cart.getUser().getId());
            assertEquals("newuser", cart.getUser().getUsername());
        }

        @Test
        @DisplayName("Should maintain one-to-one relationship with user")
        void shouldMaintainOneToOneRelationshipWithUser() {
            assertNotNull(cart.getUser());
            assertEquals(user.getId(), cart.getUser().getId());
        }

        @Test
        @DisplayName("Should handle user change")
        void shouldHandleUserChange() {
            User originalUser = cart.getUser();
            assertEquals(1L, originalUser.getId());

            User newUser = new User();
            newUser.setId(2L);
            cart.setUser(newUser);

            assertEquals(2L, cart.getUser().getId());
            assertNotEquals(originalUser.getId(), cart.getUser().getId());
        }

        @Test
        @DisplayName("Should handle null user")
        void shouldHandleNullUser() {
            cart.setUser(null);
            assertNull(cart.getUser());
        }
    }

    @Nested
    @DisplayName("Cart Items Management Tests")
    class CartItemsManagementTests {

        @Test
        @DisplayName("Should add cart item to cart")
        void shouldAddCartItemToCart() {
            cart.getCartItems().add(cartItem);

            assertEquals(1, cart.getCartItems().size());
            assertTrue(cart.getCartItems().contains(cartItem));
        }

        @Test
        @DisplayName("Should add multiple cart items")
        void shouldAddMultipleCartItems() {
            CartItem item2 = new CartItem();
            item2.setId(2L);
            item2.setCart(cart);
            item2.setProduct(product);
            item2.setQuantity(1);
            item2.setPriceAtAddition(new BigDecimal("49.99"));

            cart.getCartItems().add(cartItem);
            cart.getCartItems().add(item2);

            assertEquals(2, cart.getCartItems().size());
            assertTrue(cart.getCartItems().contains(cartItem));
            assertTrue(cart.getCartItems().contains(item2));
        }

        @Test
        @DisplayName("Should remove cart item from cart")
        void shouldRemoveCartItemFromCart() {
            cart.getCartItems().add(cartItem);
            assertEquals(1, cart.getCartItems().size());

            cart.getCartItems().remove(cartItem);
            assertEquals(0, cart.getCartItems().size());
            assertFalse(cart.getCartItems().contains(cartItem));
        }

        @Test
        @DisplayName("Should clear all cart items")
        void shouldClearAllCartItems() {
            cart.getCartItems().add(cartItem);
            CartItem item2 = new CartItem();
            cart.getCartItems().add(item2);

            assertEquals(2, cart.getCartItems().size());

            cart.getCartItems().clear();
            assertEquals(0, cart.getCartItems().size());
            assertTrue(cart.getCartItems().isEmpty());
        }

        @Test
        @DisplayName("Should replace cart items list")
        void shouldReplaceCartItemsList() {
            cart.getCartItems().add(cartItem);
            assertEquals(1, cart.getCartItems().size());

            ArrayList<CartItem> newList = new ArrayList<>();
            cart.setCartItems(newList);

            assertEquals(0, cart.getCartItems().size());
            assertTrue(cart.getCartItems().isEmpty());
        }

        @Test
        @DisplayName("Should handle empty cart items list")
        void shouldHandleEmptyCartItemsList() {
            assertEquals(0, cart.getCartItems().size());
            assertTrue(cart.getCartItems().isEmpty());
        }
    }

    @Nested
    @DisplayName("Timestamp Tests")
    class TimestampTests {

        @Test
        @DisplayName("Should set and get createdAt timestamp")
        void shouldSetAndGetCreatedAtTimestamp() {
            LocalDateTime now = LocalDateTime.now();
            cart.setCreatedAt(now);
            assertEquals(now, cart.getCreatedAt());
        }

        @Test
        @DisplayName("Should set and get updatedAt timestamp")
        void shouldSetAndGetUpdatedAtTimestamp() {
            LocalDateTime now = LocalDateTime.now();
            cart.setUpdatedAt(now);
            assertEquals(now, cart.getUpdatedAt());
        }

        @Test
        @DisplayName("Should maintain createdAt while updating updatedAt")
        void shouldMaintainCreatedAtWhileUpdatingUpdatedAt() throws InterruptedException {
            LocalDateTime created = LocalDateTime.now();
            cart.setCreatedAt(created);

            Thread.sleep(10);

            LocalDateTime updated = LocalDateTime.now();
            cart.setUpdatedAt(updated);

            assertEquals(created, cart.getCreatedAt());
            assertTrue(cart.getUpdatedAt().isAfter(cart.getCreatedAt()));
        }

        @Test
        @DisplayName("Should handle null timestamps")
        void shouldHandleNullTimestamps() {
            cart.setCreatedAt(null);
            cart.setUpdatedAt(null);

            assertNull(cart.getCreatedAt());
            assertNull(cart.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should represent complete cart")
        void shouldRepresentCompleteCart() {
            cart.getCartItems().add(cartItem);

            assertNotNull(cart.getId());
            assertNotNull(cart.getUser());
            assertNotNull(cart.getCartItems());
            assertNotNull(cart.getCreatedAt());
            assertNotNull(cart.getUpdatedAt());
            assertFalse(cart.getCartItems().isEmpty());
        }

        @Test
        @DisplayName("Should handle cart lifecycle - creation")
        void shouldHandleCartLifecycleCreation() {
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setCreatedAt(LocalDateTime.now());
            newCart.setUpdatedAt(LocalDateTime.now());

            assertNotNull(newCart.getUser());
            assertTrue(newCart.getCartItems().isEmpty());
        }

        @Test
        @DisplayName("Should handle cart lifecycle - adding items")
        void shouldHandleCartLifecycleAddingItems() {
            assertTrue(cart.getCartItems().isEmpty());

            cart.getCartItems().add(cartItem);
            cart.setUpdatedAt(LocalDateTime.now());

            assertEquals(1, cart.getCartItems().size());
        }

        @Test
        @DisplayName("Should handle cart lifecycle - checkout and clear")
        void shouldHandleCartLifecycleCheckoutAndClear() {
            cart.getCartItems().add(cartItem);
            assertEquals(1, cart.getCartItems().size());

            // Simulate checkout
            cart.getCartItems().clear();
            cart.setUpdatedAt(LocalDateTime.now());

            assertEquals(0, cart.getCartItems().size());
            assertTrue(cart.getCartItems().isEmpty());
        }

        @Test
        @DisplayName("Should handle cart with multiple items of different products")
        void shouldHandleCartWithMultipleItemsOfDifferentProducts() {
            Product product2 = new Product();
            product2.setId(2L);
            product2.setName("Product 2");
            product2.setPrice(new BigDecimal("49.99"));

            CartItem item1 = new CartItem();
            item1.setProduct(product);
            item1.setQuantity(2);
            item1.setPriceAtAddition(new BigDecimal("99.99"));

            CartItem item2 = new CartItem();
            item2.setProduct(product2);
            item2.setQuantity(3);
            item2.setPriceAtAddition(new BigDecimal("49.99"));

            cart.getCartItems().add(item1);
            cart.getCartItems().add(item2);

            assertEquals(2, cart.getCartItems().size());
        }

        @Test
        @DisplayName("Should handle cart persistence across updates")
        void shouldHandleCartPersistenceAcrossUpdates() {
            LocalDateTime originalUpdate = cart.getUpdatedAt();

            // Add item
            cart.getCartItems().add(cartItem);
            cart.setUpdatedAt(LocalDateTime.now());

            assertNotEquals(originalUpdate, cart.getUpdatedAt());
            assertEquals(1, cart.getCartItems().size());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Validation")
    class EdgeCasesAndValidation {

        @Test
        @DisplayName("Should handle cart with many items")
        void shouldHandleCartWithManyItems() {
            for (int i = 0; i < 100; i++) {
                CartItem item = new CartItem();
                item.setId((long) i);
                item.setCart(cart);
                item.setProduct(product);
                item.setQuantity(1);
                item.setPriceAtAddition(new BigDecimal("10.00"));
                cart.getCartItems().add(item);
            }

            assertEquals(100, cart.getCartItems().size());
        }

        @Test
        @DisplayName("Should handle cart with null id")
        void shouldHandleCartWithNullId() {
            Cart newCart = new Cart();
            assertNull(newCart.getId());
        }

        @Test
        @DisplayName("Should handle cart items list modification")
        void shouldHandleCartItemsListModification() {
            cart.getCartItems().add(cartItem);
            int originalSize = cart.getCartItems().size();

            // Modify list
            cart.getCartItems().remove(0);

            assertEquals(originalSize - 1, cart.getCartItems().size());
        }

        @Test
        @DisplayName("Should handle setting null cart items list")
        void shouldHandleSettingNullCartItemsList() {
            cart.setCartItems(null);
            assertNull(cart.getCartItems());
        }

        @Test
        @DisplayName("Should handle cart with all fields null except items")
        void shouldHandleCartWithAllFieldsNullExceptItems() {
            Cart nullCart = new Cart();
            assertNull(nullCart.getId());
            assertNull(nullCart.getUser());
            assertNotNull(nullCart.getCartItems());
            assertNull(nullCart.getCreatedAt());
            assertNull(nullCart.getUpdatedAt());
        }
    }
}
