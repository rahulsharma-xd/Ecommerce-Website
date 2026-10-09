package com.ecommerce.app.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CartItem entity.
 * Tests entity behavior, calculations, and business logic.
 */
@DisplayName("CartItem Entity Tests")
class CartItemTest {

    private CartItem cartItem;
    private Cart cart;
    private Product product;
    private User user;

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

        // Setup product
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
        @DisplayName("Should create cart item with default constructor")
        void shouldCreateCartItemWithDefaultConstructor() {
            CartItem newItem = new CartItem();
            assertNotNull(newItem);
            assertNull(newItem.getId());
            assertNull(newItem.getCart());
            assertNull(newItem.getProduct());
            assertNull(newItem.getQuantity());
        }

        @Test
        @DisplayName("Should set and get all basic fields")
        void shouldSetAndGetAllBasicFields() {
            assertEquals(1L, cartItem.getId());
            assertEquals(cart, cartItem.getCart());
            assertEquals(product, cartItem.getProduct());
            assertEquals(2, cartItem.getQuantity());
            assertEquals(0, new BigDecimal("99.99").compareTo(cartItem.getPriceAtAddition()));
        }
    }

    @Nested
    @DisplayName("Cart-Item Relationship Tests")
    class CartItemRelationshipTests {

        @Test
        @DisplayName("Should set and get cart")
        void shouldSetAndGetCart() {
            Cart newCart = new Cart();
            newCart.setId(2L);

            cartItem.setCart(newCart);

            assertEquals(newCart, cartItem.getCart());
            assertEquals(2L, cartItem.getCart().getId());
        }

        @Test
        @DisplayName("Should maintain many-to-one relationship with cart")
        void shouldMaintainManyToOneRelationshipWithCart() {
            assertNotNull(cartItem.getCart());
            assertEquals(cart.getId(), cartItem.getCart().getId());
        }

        @Test
        @DisplayName("Should handle null cart")
        void shouldHandleNullCart() {
            cartItem.setCart(null);
            assertNull(cartItem.getCart());
        }
    }

    @Nested
    @DisplayName("Product-Item Relationship Tests")
    class ProductItemRelationshipTests {

        @Test
        @DisplayName("Should set and get product")
        void shouldSetAndGetProduct() {
            Product newProduct = new Product();
            newProduct.setId(2L);
            newProduct.setName("New Product");

            cartItem.setProduct(newProduct);

            assertEquals(newProduct, cartItem.getProduct());
            assertEquals(2L, cartItem.getProduct().getId());
        }

        @Test
        @DisplayName("Should maintain many-to-one relationship with product")
        void shouldMaintainManyToOneRelationshipWithProduct() {
            assertNotNull(cartItem.getProduct());
            assertEquals(product.getId(), cartItem.getProduct().getId());
            assertEquals("Test Product", cartItem.getProduct().getName());
        }

        @Test
        @DisplayName("Should handle null product")
        void shouldHandleNullProduct() {
            cartItem.setProduct(null);
            assertNull(cartItem.getProduct());
        }
    }

    @Nested
    @DisplayName("Quantity Management Tests")
    class QuantityManagementTests {

        @Test
        @DisplayName("Should set and get quantity")
        void shouldSetAndGetQuantity() {
            cartItem.setQuantity(5);
            assertEquals(5, cartItem.getQuantity());
        }

        @Test
        @DisplayName("Should handle quantity increase")
        void shouldHandleQuantityIncrease() {
            cartItem.setQuantity(2);
            assertEquals(2, cartItem.getQuantity());

            cartItem.setQuantity(5);
            assertEquals(5, cartItem.getQuantity());
        }

        @Test
        @DisplayName("Should handle quantity decrease")
        void shouldHandleQuantityDecrease() {
            cartItem.setQuantity(5);
            assertEquals(5, cartItem.getQuantity());

            cartItem.setQuantity(2);
            assertEquals(2, cartItem.getQuantity());
        }

        @Test
        @DisplayName("Should handle quantity of 1")
        void shouldHandleQuantityOfOne() {
            cartItem.setQuantity(1);
            assertEquals(1, cartItem.getQuantity());
        }

        @Test
        @DisplayName("Should handle large quantities")
        void shouldHandleLargeQuantities() {
            cartItem.setQuantity(999);
            assertEquals(999, cartItem.getQuantity());
        }

        @Test
        @DisplayName("Should handle null quantity")
        void shouldHandleNullQuantity() {
            cartItem.setQuantity(null);
            assertNull(cartItem.getQuantity());
        }
    }

    @Nested
    @DisplayName("Price Management Tests")
    class PriceManagementTests {

        @Test
        @DisplayName("Should set and get price at addition")
        void shouldSetAndGetPriceAtAddition() {
            cartItem.setPriceAtAddition(new BigDecimal("149.99"));
            assertEquals(0, new BigDecimal("149.99").compareTo(cartItem.getPriceAtAddition()));
        }

        @Test
        @DisplayName("Should store price snapshot at addition time")
        void shouldStorePriceSnapshotAtAdditionTime() {
            // Price at addition
            cartItem.setPriceAtAddition(new BigDecimal("99.99"));
            assertEquals(0, new BigDecimal("99.99").compareTo(cartItem.getPriceAtAddition()));

            // Product price changes (simulated)
            product.setPrice(new BigDecimal("79.99"));

            // Cart item price should remain unchanged
            assertEquals(0, new BigDecimal("99.99").compareTo(cartItem.getPriceAtAddition()));
            assertNotEquals(product.getPrice(), cartItem.getPriceAtAddition());
        }

        @Test
        @DisplayName("Should handle price with decimals")
        void shouldHandlePriceWithDecimals() {
            cartItem.setPriceAtAddition(new BigDecimal("19.99"));
            assertEquals(0, new BigDecimal("19.99").compareTo(cartItem.getPriceAtAddition()));
        }

        @Test
        @DisplayName("Should handle zero price")
        void shouldHandleZeroPrice() {
            cartItem.setPriceAtAddition(BigDecimal.ZERO);
            assertEquals(0, BigDecimal.ZERO.compareTo(cartItem.getPriceAtAddition()));
        }

        @Test
        @DisplayName("Should handle null price")
        void shouldHandleNullPrice() {
            cartItem.setPriceAtAddition(null);
            assertNull(cartItem.getPriceAtAddition());
        }
    }

    @Nested
    @DisplayName("Subtotal Calculation Tests")
    class SubtotalCalculationTests {

        @Test
        @DisplayName("Should calculate subtotal correctly")
        void shouldCalculateSubtotalCorrectly() {
            cartItem.setQuantity(2);
            cartItem.setPriceAtAddition(new BigDecimal("99.99"));

            BigDecimal expectedSubtotal = new BigDecimal("199.98");
            BigDecimal actualSubtotal = cartItem.getSubtotal();

            assertEquals(0, expectedSubtotal.compareTo(actualSubtotal));
        }

        @Test
        @DisplayName("Should calculate subtotal for quantity of 1")
        void shouldCalculateSubtotalForQuantityOfOne() {
            cartItem.setQuantity(1);
            cartItem.setPriceAtAddition(new BigDecimal("99.99"));

            BigDecimal expectedSubtotal = new BigDecimal("99.99");
            assertEquals(0, expectedSubtotal.compareTo(cartItem.getSubtotal()));
        }

        @Test
        @DisplayName("Should calculate subtotal for large quantity")
        void shouldCalculateSubtotalForLargeQuantity() {
            cartItem.setQuantity(100);
            cartItem.setPriceAtAddition(new BigDecimal("10.00"));

            BigDecimal expectedSubtotal = new BigDecimal("1000.00");
            assertEquals(0, expectedSubtotal.compareTo(cartItem.getSubtotal()));
        }

        @Test
        @DisplayName("Should recalculate subtotal when quantity changes")
        void shouldRecalculateSubtotalWhenQuantityChanges() {
            cartItem.setQuantity(2);
            cartItem.setPriceAtAddition(new BigDecimal("50.00"));

            BigDecimal subtotal1 = cartItem.getSubtotal();
            assertEquals(0, new BigDecimal("100.00").compareTo(subtotal1));

            cartItem.setQuantity(5);
            BigDecimal subtotal2 = cartItem.getSubtotal();
            assertEquals(0, new BigDecimal("250.00").compareTo(subtotal2));
        }

        @Test
        @DisplayName("Should calculate subtotal with decimal prices")
        void shouldCalculateSubtotalWithDecimalPrices() {
            cartItem.setQuantity(3);
            cartItem.setPriceAtAddition(new BigDecimal("19.99"));

            BigDecimal expectedSubtotal = new BigDecimal("59.97");
            assertEquals(0, expectedSubtotal.compareTo(cartItem.getSubtotal()));
        }

        @Test
        @DisplayName("Should calculate zero subtotal for zero price")
        void shouldCalculateZeroSubtotalForZeroPrice() {
            cartItem.setQuantity(5);
            cartItem.setPriceAtAddition(BigDecimal.ZERO);

            assertEquals(0, BigDecimal.ZERO.compareTo(cartItem.getSubtotal()));
        }
    }

    @Nested
    @DisplayName("Timestamp Tests")
    class TimestampTests {

        @Test
        @DisplayName("Should set and get createdAt timestamp")
        void shouldSetAndGetCreatedAtTimestamp() {
            LocalDateTime now = LocalDateTime.now();
            cartItem.setCreatedAt(now);
            assertEquals(now, cartItem.getCreatedAt());
        }

        @Test
        @DisplayName("Should set and get updatedAt timestamp")
        void shouldSetAndGetUpdatedAtTimestamp() {
            LocalDateTime now = LocalDateTime.now();
            cartItem.setUpdatedAt(now);
            assertEquals(now, cartItem.getUpdatedAt());
        }

        @Test
        @DisplayName("Should maintain createdAt while updating updatedAt")
        void shouldMaintainCreatedAtWhileUpdatingUpdatedAt() throws InterruptedException {
            LocalDateTime created = LocalDateTime.now();
            cartItem.setCreatedAt(created);

            Thread.sleep(10);

            LocalDateTime updated = LocalDateTime.now();
            cartItem.setUpdatedAt(updated);

            assertEquals(created, cartItem.getCreatedAt());
            assertTrue(cartItem.getUpdatedAt().isAfter(cartItem.getCreatedAt()));
        }

        @Test
        @DisplayName("Should handle null timestamps")
        void shouldHandleNullTimestamps() {
            cartItem.setCreatedAt(null);
            cartItem.setUpdatedAt(null);

            assertNull(cartItem.getCreatedAt());
            assertNull(cartItem.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should represent complete cart item")
        void shouldRepresentCompleteCartItem() {
            assertNotNull(cartItem.getId());
            assertNotNull(cartItem.getCart());
            assertNotNull(cartItem.getProduct());
            assertNotNull(cartItem.getQuantity());
            assertNotNull(cartItem.getPriceAtAddition());
            assertNotNull(cartItem.getCreatedAt());
            assertNotNull(cartItem.getUpdatedAt());
        }

        @Test
        @DisplayName("Should handle cart item lifecycle - creation")
        void shouldHandleCartItemLifecycleCreation() {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(1);
            newItem.setPriceAtAddition(product.getPrice());
            newItem.setCreatedAt(LocalDateTime.now());
            newItem.setUpdatedAt(LocalDateTime.now());

            assertEquals(1, newItem.getQuantity());
            assertEquals(product.getPrice(), newItem.getPriceAtAddition());
        }

        @Test
        @DisplayName("Should handle cart item lifecycle - quantity update")
        void shouldHandleCartItemLifecycleQuantityUpdate() {
            cartItem.setQuantity(2);
            BigDecimal subtotal1 = cartItem.getSubtotal();

            cartItem.setQuantity(5);
            cartItem.setUpdatedAt(LocalDateTime.now());
            BigDecimal subtotal2 = cartItem.getSubtotal();

            assertNotEquals(subtotal1, subtotal2);
            assertEquals(5, cartItem.getQuantity());
        }

        @Test
        @DisplayName("Should handle cart item lifecycle - removal")
        void shouldHandleCartItemLifecycleRemoval() {
            cart.getCartItems().add(cartItem);
            assertEquals(1, cart.getCartItems().size());

            cart.getCartItems().remove(cartItem);
            assertEquals(0, cart.getCartItems().size());
        }

        @Test
        @DisplayName("Should maintain price independence from product")
        void shouldMaintainPriceIndependenceFromProduct() {
            BigDecimal originalPrice = new BigDecimal("99.99");
            cartItem.setPriceAtAddition(originalPrice);

            // Product price changes
            product.setPrice(new BigDecimal("149.99"));

            // Cart item price should remain unchanged
            assertEquals(0, originalPrice.compareTo(cartItem.getPriceAtAddition()));
        }

        @Test
        @DisplayName("Should handle multiple cart items for same product in same cart")
        void shouldHandleMultipleCartItemsForSameProductInSameCart() {
            CartItem item1 = new CartItem();
            item1.setId(1L);
            item1.setCart(cart);
            item1.setProduct(product);
            item1.setQuantity(2);
            item1.setPriceAtAddition(new BigDecimal("99.99"));

            CartItem item2 = new CartItem();
            item2.setId(2L);
            item2.setCart(cart);
            item2.setProduct(product);
            item2.setQuantity(3);
            item2.setPriceAtAddition(new BigDecimal("89.99")); // Different price snapshot

            assertNotEquals(item1.getId(), item2.getId());
            assertEquals(item1.getProduct(), item2.getProduct());
            assertNotEquals(item1.getPriceAtAddition(), item2.getPriceAtAddition());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Validation")
    class EdgeCasesAndValidation {

        @Test
        @DisplayName("Should handle cart item with minimum values")
        void shouldHandleCartItemWithMinimumValues() {
            CartItem minItem = new CartItem();
            minItem.setCart(cart);
            minItem.setProduct(product);
            minItem.setQuantity(1);
            minItem.setPriceAtAddition(new BigDecimal("0.01"));

            assertEquals(1, minItem.getQuantity());
            assertEquals(0, new BigDecimal("0.01").compareTo(minItem.getPriceAtAddition()));
        }

        @Test
        @DisplayName("Should handle cart item with large values")
        void shouldHandleCartItemWithLargeValues() {
            cartItem.setQuantity(9999);
            cartItem.setPriceAtAddition(new BigDecimal("99999.99"));

            assertEquals(9999, cartItem.getQuantity());
            BigDecimal largeSubtotal = cartItem.getSubtotal();
            assertTrue(largeSubtotal.compareTo(BigDecimal.ZERO) > 0);
        }

        @Test
        @DisplayName("Should handle cart item with null id")
        void shouldHandleCartItemWithNullId() {
            CartItem newItem = new CartItem();
            assertNull(newItem.getId());
        }

        @Test
        @DisplayName("Should handle cart item with negative quantity")
        void shouldHandleCartItemWithNegativeQuantity() {
            cartItem.setQuantity(-1);
            assertEquals(-1, cartItem.getQuantity());
        }

        @Test
        @DisplayName("Should handle cart item with all fields null")
        void shouldHandleCartItemWithAllFieldsNull() {
            CartItem nullItem = new CartItem();
            assertNull(nullItem.getId());
            assertNull(nullItem.getCart());
            assertNull(nullItem.getProduct());
            assertNull(nullItem.getQuantity());
            assertNull(nullItem.getPriceAtAddition());
            assertNull(nullItem.getCreatedAt());
            assertNull(nullItem.getUpdatedAt());
        }
    }
}
