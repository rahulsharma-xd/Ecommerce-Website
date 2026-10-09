package com.ecommerce.app.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OrderItem entity.
 * Tests entity behavior, calculations, and relationships.
 */
@DisplayName("OrderItem Entity Tests")
class OrderItemTest {

    private OrderItem orderItem;
    private Order order;
    private Product product;

    @BeforeEach
    void setUp() {
        // Setup test order
        order = new Order();
        order.setId(1L);
        order.setOrderNumber("ORD-2025-001");
        order.setStatus(OrderStatus.PENDING);

        // Setup test product
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("99.99"));

        // Setup test order item
        orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setPriceAtPurchase(new BigDecimal("99.99"));
        orderItem.setSubtotal(new BigDecimal("199.98"));
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create order item with default constructor")
        void shouldCreateOrderItemWithDefaultConstructor() {
            OrderItem newItem = new OrderItem();
            assertNotNull(newItem);
            assertNull(newItem.getId());
            assertNull(newItem.getOrder());
            assertNull(newItem.getProduct());
        }

        @Test
        @DisplayName("Should create order item with parameterized constructor")
        void shouldCreateOrderItemWithParameterizedConstructor() {
            OrderItem newItem = new OrderItem(order, product, 3, new BigDecimal("49.99"));

            assertNotNull(newItem);
            assertEquals(order, newItem.getOrder());
            assertEquals(product, newItem.getProduct());
            assertEquals(3, newItem.getQuantity());
            assertEquals(new BigDecimal("49.99"), newItem.getPriceAtPurchase());
            // Subtotal should be calculated automatically
            assertEquals(new BigDecimal("149.97"), newItem.getSubtotal());
        }

        @Test
        @DisplayName("Should calculate subtotal correctly in constructor")
        void shouldCalculateSubtotalCorrectlyInConstructor() {
            OrderItem item1 = new OrderItem(order, product, 1, new BigDecimal("10.00"));
            assertEquals(new BigDecimal("10.00"), item1.getSubtotal());

            OrderItem item2 = new OrderItem(order, product, 5, new BigDecimal("20.50"));
            assertEquals(new BigDecimal("102.50"), item2.getSubtotal());

            OrderItem item3 = new OrderItem(order, product, 10, new BigDecimal("99.99"));
            assertEquals(new BigDecimal("999.90"), item3.getSubtotal());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get id correctly")
        void shouldSetAndGetId() {
            orderItem.setId(100L);
            assertEquals(100L, orderItem.getId());
        }

        @Test
        @DisplayName("Should set and get order correctly")
        void shouldSetAndGetOrder() {
            Order newOrder = new Order();
            newOrder.setId(2L);
            orderItem.setOrder(newOrder);
            assertEquals(newOrder, orderItem.getOrder());
        }

        @Test
        @DisplayName("Should set and get product correctly")
        void shouldSetAndGetProduct() {
            Product newProduct = new Product();
            newProduct.setId(2L);
            newProduct.setName("New Product");
            orderItem.setProduct(newProduct);
            assertEquals(newProduct, orderItem.getProduct());
        }

        @Test
        @DisplayName("Should set and get quantity correctly")
        void shouldSetAndGetQuantity() {
            orderItem.setQuantity(5);
            assertEquals(5, orderItem.getQuantity());
        }

        @Test
        @DisplayName("Should set and get price at purchase correctly")
        void shouldSetAndGetPriceAtPurchase() {
            BigDecimal newPrice = new BigDecimal("149.99");
            orderItem.setPriceAtPurchase(newPrice);
            assertEquals(newPrice, orderItem.getPriceAtPurchase());
        }

        @Test
        @DisplayName("Should set and get subtotal correctly")
        void shouldSetAndGetSubtotal() {
            BigDecimal newSubtotal = new BigDecimal("299.98");
            orderItem.setSubtotal(newSubtotal);
            assertEquals(newSubtotal, orderItem.getSubtotal());
        }
    }

    @Nested
    @DisplayName("Subtotal Calculation Tests")
    class SubtotalCalculationTests {

        @Test
        @DisplayName("Should recalculate subtotal when quantity changes")
        void shouldRecalculateSubtotalWhenQuantityChanges() {
            orderItem.setPriceAtPurchase(new BigDecimal("50.00"));
            orderItem.setQuantity(3);
            assertEquals(new BigDecimal("150.00"), orderItem.getSubtotal());

            orderItem.setQuantity(5);
            assertEquals(new BigDecimal("250.00"), orderItem.getSubtotal());
        }

        @Test
        @DisplayName("Should recalculate subtotal when price changes")
        void shouldRecalculateSubtotalWhenPriceChanges() {
            orderItem.setQuantity(2);
            orderItem.setPriceAtPurchase(new BigDecimal("25.00"));
            assertEquals(new BigDecimal("50.00"), orderItem.getSubtotal());

            orderItem.setPriceAtPurchase(new BigDecimal("30.00"));
            assertEquals(new BigDecimal("60.00"), orderItem.getSubtotal());
        }

        @Test
        @DisplayName("Should calculate subtotal using helper method")
        void shouldCalculateSubtotalUsingHelperMethod() {
            orderItem.setQuantity(4);
            orderItem.setPriceAtPurchase(new BigDecimal("12.50"));
            orderItem.calculateSubtotal();
            assertEquals(new BigDecimal("50.00"), orderItem.getSubtotal());
        }

        @Test
        @DisplayName("Should handle single quantity")
        void shouldHandleSingleQuantity() {
            orderItem.setQuantity(1);
            orderItem.setPriceAtPurchase(new BigDecimal("99.99"));
            assertEquals(new BigDecimal("99.99"), orderItem.getSubtotal());
        }

        @Test
        @DisplayName("Should handle decimal prices correctly")
        void shouldHandleDecimalPricesCorrectly() {
            orderItem.setQuantity(3);
            orderItem.setPriceAtPurchase(new BigDecimal("19.99"));
            assertEquals(new BigDecimal("59.97"), orderItem.getSubtotal());
        }

        @Test
        @DisplayName("Should maintain precision in calculations")
        void shouldMaintainPrecisionInCalculations() {
            orderItem.setQuantity(7);
            orderItem.setPriceAtPurchase(new BigDecimal("12.345"));
            BigDecimal expected = new BigDecimal("86.415");
            assertEquals(expected, orderItem.getSubtotal());
        }
    }

    @Nested
    @DisplayName("Price Snapshot Tests")
    class PriceSnapshotTests {

        @Test
        @DisplayName("Should preserve price at purchase even if product price changes")
        void shouldPreservePriceAtPurchaseEvenIfProductPriceChanges() {
            BigDecimal originalPrice = new BigDecimal("99.99");
            orderItem.setPriceAtPurchase(originalPrice);

            // Simulate product price change
            product.setPrice(new BigDecimal("149.99"));

            // Order item should still have original price
            assertEquals(originalPrice, orderItem.getPriceAtPurchase());
        }

        @Test
        @DisplayName("Should use price at purchase for subtotal calculation")
        void shouldUsePriceAtPurchaseForSubtotalCalculation() {
            orderItem.setQuantity(2);
            orderItem.setPriceAtPurchase(new BigDecimal("50.00"));

            // Even if product price is different
            product.setPrice(new BigDecimal("75.00"));

            // Subtotal should use priceAtPurchase, not product.price
            assertEquals(new BigDecimal("100.00"), orderItem.getSubtotal());
        }

        @Test
        @DisplayName("Should allow different price at purchase than current product price")
        void shouldAllowDifferentPriceAtPurchaseThanCurrentProductPrice() {
            product.setPrice(new BigDecimal("100.00"));
            orderItem.setPriceAtPurchase(new BigDecimal("80.00")); // Sale price

            assertNotEquals(product.getPrice(), orderItem.getPriceAtPurchase());
            assertEquals(new BigDecimal("80.00"), orderItem.getPriceAtPurchase());
        }
    }

    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("Should maintain reference to order")
        void shouldMaintainReferenceToOrder() {
            assertNotNull(orderItem.getOrder());
            assertEquals(order, orderItem.getOrder());
            assertEquals("ORD-2025-001", orderItem.getOrder().getOrderNumber());
        }

        @Test
        @DisplayName("Should maintain reference to product")
        void shouldMaintainReferenceToProduct() {
            assertNotNull(orderItem.getProduct());
            assertEquals(product, orderItem.getProduct());
            assertEquals("Test Product", orderItem.getProduct().getName());
        }

        @Test
        @DisplayName("Should allow null order temporarily")
        void shouldAllowNullOrderTemporarily() {
            orderItem.setOrder(null);
            assertNull(orderItem.getOrder());
        }

        @Test
        @DisplayName("Should allow null product temporarily")
        void shouldAllowNullProductTemporarily() {
            orderItem.setProduct(null);
            assertNull(orderItem.getProduct());
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should handle large quantities")
        void shouldHandleLargeQuantities() {
            orderItem.setQuantity(1000);
            orderItem.setPriceAtPurchase(new BigDecimal("5.99"));
            assertEquals(new BigDecimal("5990.00"), orderItem.getSubtotal());
        }

        @Test
        @DisplayName("Should handle expensive items")
        void shouldHandleExpensiveItems() {
            orderItem.setQuantity(1);
            orderItem.setPriceAtPurchase(new BigDecimal("9999.99"));
            assertEquals(new BigDecimal("9999.99"), orderItem.getSubtotal());
        }

        @Test
        @DisplayName("Should handle cheap items")
        void shouldHandleCheapItems() {
            orderItem.setQuantity(10);
            orderItem.setPriceAtPurchase(new BigDecimal("0.99"));
            assertEquals(new BigDecimal("9.90"), orderItem.getSubtotal());
        }

        @Test
        @DisplayName("Should not allow negative subtotal calculation")
        void shouldHandleEdgeCaseCalculations() {
            orderItem.setQuantity(0);
            orderItem.setPriceAtPurchase(new BigDecimal("10.00"));
            assertEquals(new BigDecimal("0.00"), orderItem.getSubtotal());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should throw NullPointerException when setting null price with existing quantity")
        void shouldThrowNullPointerWhenSettingNullPriceWithExistingQuantity() {
            orderItem.setQuantity(5);
            // Actual behavior: NPE when trying to recalculate subtotal with null price
            assertThrows(NullPointerException.class, () -> {
                orderItem.setPriceAtPurchase(null);
            });
        }

        @Test
        @DisplayName("Should throw NullPointerException when setting null quantity with existing price")
        void shouldThrowNullPointerWhenSettingNullQuantityWithExistingPrice() {
            orderItem.setPriceAtPurchase(new BigDecimal("10.00"));
            // Actual behavior: NPE when trying to recalculate subtotal with null quantity
            assertThrows(NullPointerException.class, () -> {
                orderItem.setQuantity(null);
            });
        }

        @Test
        @DisplayName("Should handle zero price")
        void shouldHandleZeroPrice() {
            orderItem.setQuantity(5);
            orderItem.setPriceAtPurchase(BigDecimal.ZERO);
            // BigDecimal.ZERO and new BigDecimal("0.00") are different scales
            assertEquals(0, orderItem.getSubtotal().compareTo(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("Should handle very precise decimal prices")
        void shouldHandleVeryPreciseDecimalPrices() {
            orderItem.setQuantity(3);
            orderItem.setPriceAtPurchase(new BigDecimal("12.3456789"));
            BigDecimal expected = new BigDecimal("37.0370367");
            assertEquals(expected, orderItem.getSubtotal());
        }
    }
}
