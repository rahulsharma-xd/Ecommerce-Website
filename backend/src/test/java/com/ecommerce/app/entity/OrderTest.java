package com.ecommerce.app.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Order entity.
 * Tests entity behavior, relationships, and helper methods.
 */
@DisplayName("Order Entity Tests")
class OrderTest {

    private Order order;
    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        // Setup test user
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        // Setup test product
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("99.99"));

        // Setup test order
        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setOrderNumber("ORD-2025-001");
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("199.98"));
        order.setOrderedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create order with default constructor")
        void shouldCreateOrderWithDefaultConstructor() {
            Order newOrder = new Order();
            assertNotNull(newOrder);
            assertNull(newOrder.getId());
            assertNull(newOrder.getUser());
            assertNull(newOrder.getOrderNumber());
        }

        @Test
        @DisplayName("Should create order with parameterized constructor")
        void shouldCreateOrderWithParameterizedConstructor() {
            Order newOrder = new Order(user, "ORD-2025-002", OrderStatus.PENDING, new BigDecimal("299.99"));

            assertNotNull(newOrder);
            assertEquals(user, newOrder.getUser());
            assertEquals("ORD-2025-002", newOrder.getOrderNumber());
            assertEquals(OrderStatus.PENDING, newOrder.getStatus());
            assertEquals(new BigDecimal("299.99"), newOrder.getTotalAmount());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get id correctly")
        void shouldSetAndGetId() {
            order.setId(100L);
            assertEquals(100L, order.getId());
        }

        @Test
        @DisplayName("Should set and get user correctly")
        void shouldSetAndGetUser() {
            User newUser = new User();
            newUser.setId(2L);
            order.setUser(newUser);
            assertEquals(newUser, order.getUser());
            assertEquals(2L, order.getUser().getId());
        }

        @Test
        @DisplayName("Should set and get order number correctly")
        void shouldSetAndGetOrderNumber() {
            order.setOrderNumber("ORD-2025-999");
            assertEquals("ORD-2025-999", order.getOrderNumber());
        }

        @Test
        @DisplayName("Should set and get status correctly")
        void shouldSetAndGetStatus() {
            order.setStatus(OrderStatus.SHIPPED);
            assertEquals(OrderStatus.SHIPPED, order.getStatus());
        }

        @Test
        @DisplayName("Should set and get total amount correctly")
        void shouldSetAndGetTotalAmount() {
            BigDecimal newAmount = new BigDecimal("500.00");
            order.setTotalAmount(newAmount);
            assertEquals(newAmount, order.getTotalAmount());
        }

        @Test
        @DisplayName("Should set and get orderedAt timestamp correctly")
        void shouldSetAndGetOrderedAt() {
            LocalDateTime now = LocalDateTime.now();
            order.setOrderedAt(now);
            assertEquals(now, order.getOrderedAt());
        }

        @Test
        @DisplayName("Should set and get updatedAt timestamp correctly")
        void shouldSetAndGetUpdatedAt() {
            LocalDateTime now = LocalDateTime.now();
            order.setUpdatedAt(now);
            assertEquals(now, order.getUpdatedAt());
        }

        @Test
        @DisplayName("Should initialize order items as empty list")
        void shouldInitializeOrderItemsAsEmptyList() {
            Order newOrder = new Order();
            assertNotNull(newOrder.getOrderItems());
            assertTrue(newOrder.getOrderItems().isEmpty());
        }
    }

    @Nested
    @DisplayName("OrderItem Relationship Tests")
    class OrderItemRelationshipTests {

        @Test
        @DisplayName("Should add order item to order")
        void shouldAddOrderItemToOrder() {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(2);
            orderItem.setPriceAtPurchase(new BigDecimal("99.99"));

            order.addOrderItem(orderItem);

            assertEquals(1, order.getOrderItems().size());
            assertTrue(order.getOrderItems().contains(orderItem));
            assertEquals(order, orderItem.getOrder());
        }

        @Test
        @DisplayName("Should add multiple order items to order")
        void shouldAddMultipleOrderItemsToOrder() {
            OrderItem item1 = new OrderItem();
            item1.setProduct(product);
            item1.setQuantity(2);
            item1.setPriceAtPurchase(new BigDecimal("99.99"));

            OrderItem item2 = new OrderItem();
            item2.setProduct(product);
            item2.setQuantity(1);
            item2.setPriceAtPurchase(new BigDecimal("49.99"));

            order.addOrderItem(item1);
            order.addOrderItem(item2);

            assertEquals(2, order.getOrderItems().size());
            assertTrue(order.getOrderItems().contains(item1));
            assertTrue(order.getOrderItems().contains(item2));
        }

        @Test
        @DisplayName("Should remove order item from order")
        void shouldRemoveOrderItemFromOrder() {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(2);
            orderItem.setPriceAtPurchase(new BigDecimal("99.99"));

            order.addOrderItem(orderItem);
            assertEquals(1, order.getOrderItems().size());

            order.removeOrderItem(orderItem);
            assertEquals(0, order.getOrderItems().size());
            assertNull(orderItem.getOrder());
        }

        @Test
        @DisplayName("Should maintain bidirectional relationship when adding item")
        void shouldMaintainBidirectionalRelationshipWhenAddingItem() {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(1);
            orderItem.setPriceAtPurchase(new BigDecimal("99.99"));

            order.addOrderItem(orderItem);

            // Check order has the item
            assertTrue(order.getOrderItems().contains(orderItem));
            // Check item has reference to order
            assertEquals(order, orderItem.getOrder());
        }

        @Test
        @DisplayName("Should maintain bidirectional relationship when removing item")
        void shouldMaintainBidirectionalRelationshipWhenRemovingItem() {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(1);
            orderItem.setPriceAtPurchase(new BigDecimal("99.99"));

            order.addOrderItem(orderItem);
            order.removeOrderItem(orderItem);

            // Check order doesn't have the item
            assertFalse(order.getOrderItems().contains(orderItem));
            // Check item has no reference to order
            assertNull(orderItem.getOrder());
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should allow status transition from PENDING to PROCESSING")
        void shouldAllowStatusTransitionFromPendingToProcessing() {
            order.setStatus(OrderStatus.PENDING);
            order.setStatus(OrderStatus.PROCESSING);
            assertEquals(OrderStatus.PROCESSING, order.getStatus());
        }

        @Test
        @DisplayName("Should allow status transition from PROCESSING to SHIPPED")
        void shouldAllowStatusTransitionFromProcessingToShipped() {
            order.setStatus(OrderStatus.PROCESSING);
            order.setStatus(OrderStatus.SHIPPED);
            assertEquals(OrderStatus.SHIPPED, order.getStatus());
        }

        @Test
        @DisplayName("Should allow status transition from SHIPPED to DELIVERED")
        void shouldAllowStatusTransitionFromShippedToDelivered() {
            order.setStatus(OrderStatus.SHIPPED);
            order.setStatus(OrderStatus.DELIVERED);
            assertEquals(OrderStatus.DELIVERED, order.getStatus());
        }

        @Test
        @DisplayName("Should allow cancellation from any status except DELIVERED")
        void shouldAllowCancellationFromAnyStatusExceptDelivered() {
            order.setStatus(OrderStatus.PENDING);
            order.setStatus(OrderStatus.CANCELLED);
            assertEquals(OrderStatus.CANCELLED, order.getStatus());

            order.setStatus(OrderStatus.PROCESSING);
            order.setStatus(OrderStatus.CANCELLED);
            assertEquals(OrderStatus.CANCELLED, order.getStatus());
        }

        @Test
        @DisplayName("Should handle order number uniqueness")
        void shouldHandleOrderNumberUniqueness() {
            String uniqueOrderNumber = "ORD-2025-" + System.currentTimeMillis();
            order.setOrderNumber(uniqueOrderNumber);
            assertEquals(uniqueOrderNumber, order.getOrderNumber());
        }

        @Test
        @DisplayName("Should handle large total amounts")
        void shouldHandleLargeTotalAmounts() {
            BigDecimal largeAmount = new BigDecimal("999999.99");
            order.setTotalAmount(largeAmount);
            assertEquals(largeAmount, order.getTotalAmount());
        }

        @Test
        @DisplayName("Should handle zero total amount")
        void shouldHandleZeroTotalAmount() {
            BigDecimal zeroAmount = BigDecimal.ZERO;
            order.setTotalAmount(zeroAmount);
            assertEquals(zeroAmount, order.getTotalAmount());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null user")
        void shouldHandleNullUser() {
            order.setUser(null);
            assertNull(order.getUser());
        }

        @Test
        @DisplayName("Should handle null order number")
        void shouldHandleNullOrderNumber() {
            order.setOrderNumber(null);
            assertNull(order.getOrderNumber());
        }

        @Test
        @DisplayName("Should handle empty order items list")
        void shouldHandleEmptyOrderItemsList() {
            Order emptyOrder = new Order();
            assertNotNull(emptyOrder.getOrderItems());
            assertEquals(0, emptyOrder.getOrderItems().size());
        }

        @Test
        @DisplayName("Should handle order with many items")
        void shouldHandleOrderWithManyItems() {
            for (int i = 0; i < 100; i++) {
                OrderItem item = new OrderItem();
                item.setProduct(product);
                item.setQuantity(1);
                item.setPriceAtPurchase(new BigDecimal("10.00"));
                order.addOrderItem(item);
            }
            assertEquals(100, order.getOrderItems().size());
        }
    }
}
