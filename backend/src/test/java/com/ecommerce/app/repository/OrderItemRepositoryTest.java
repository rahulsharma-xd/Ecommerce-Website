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
 * Integration tests for OrderItemRepository.
 * Uses @DataJpaTest for repository layer testing with in-memory H2 database.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("OrderItemRepository Integration Tests")
class OrderItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Order testOrder;
    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        // Clean up database
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();

        // Create test user
        testUser = createUser("testuser", "test@example.com");
        testUser = userRepository.save(testUser);

        // Create test order
        testOrder = createOrder(testUser, "ORD-2025-001", OrderStatus.PENDING, new BigDecimal("199.98"));
        testOrder = orderRepository.save(testOrder);

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
        @DisplayName("Should save and find order item by id")
        void shouldSaveAndFindOrderItemById() {
            // Given
            OrderItem orderItem = createOrderItem(testOrder, testProduct1, 2, new BigDecimal("99.99"));

            // When
            OrderItem savedItem = orderItemRepository.save(orderItem);
            Optional<OrderItem> foundItem = orderItemRepository.findById(savedItem.getId());

            // Then
            assertThat(foundItem).isPresent();
            assertThat(foundItem.get().getQuantity()).isEqualTo(2);
            assertThat(foundItem.get().getPriceAtPurchase()).isEqualByComparingTo(new BigDecimal("99.99"));
        }

        @Test
        @DisplayName("Should save order item with all required fields")
        void shouldSaveOrderItemWithAllRequiredFields() {
            // Given
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(testOrder);
            orderItem.setProduct(testProduct1);
            orderItem.setQuantity(3);
            orderItem.setPriceAtPurchase(new BigDecimal("99.99"));

            // When
            OrderItem savedItem = orderItemRepository.save(orderItem);

            // Then
            assertThat(savedItem.getId()).isNotNull();
            assertThat(savedItem.getOrder().getId()).isEqualTo(testOrder.getId());
            assertThat(savedItem.getProduct().getId()).isEqualTo(testProduct1.getId());
            assertThat(savedItem.getQuantity()).isEqualTo(3);
            assertThat(savedItem.getPriceAtPurchase()).isEqualByComparingTo(new BigDecimal("99.99"));
            assertThat(savedItem.getSubtotal()).isEqualByComparingTo(new BigDecimal("299.97"));
        }

        @Test
        @DisplayName("Should find all order items")
        void shouldFindAllOrderItems() {
            // Given
            orderItemRepository.save(createOrderItem(testOrder, testProduct1, 2, new BigDecimal("99.99")));
            orderItemRepository.save(createOrderItem(testOrder, testProduct2, 1, new BigDecimal("49.99")));

            // When
            List<OrderItem> allItems = orderItemRepository.findAll();

            // Then
            assertThat(allItems).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Order Item Relationships")
    class OrderItemRelationships {

        @Test
        @DisplayName("Should maintain order-item relationship")
        void shouldMaintainOrderItemRelationship() {
            // Given
            OrderItem orderItem = createOrderItem(testOrder, testProduct1, 2, new BigDecimal("99.99"));

            // When
            OrderItem savedItem = orderItemRepository.save(orderItem);

            // Then
            assertThat(savedItem.getOrder()).isNotNull();
            assertThat(savedItem.getOrder().getId()).isEqualTo(testOrder.getId());
            assertThat(savedItem.getOrder().getOrderNumber()).isEqualTo("ORD-2025-001");
            assertThat(savedItem.getOrder().getUser().getId()).isEqualTo(testUser.getId());
        }

        @Test
        @DisplayName("Should maintain product-item relationship")
        void shouldMaintainProductItemRelationship() {
            // Given
            OrderItem orderItem = createOrderItem(testOrder, testProduct1, 2, new BigDecimal("99.99"));

            // When
            OrderItem savedItem = orderItemRepository.save(orderItem);

            // Then
            assertThat(savedItem.getProduct()).isNotNull();
            assertThat(savedItem.getProduct().getId()).isEqualTo(testProduct1.getId());
            assertThat(savedItem.getProduct().getName()).isEqualTo("Product 1");
        }

        @Test
        @DisplayName("Should store price at purchase time")
        void shouldStorePriceAtPurchaseTime() {
            // Given
            OrderItem orderItem = createOrderItem(testOrder, testProduct1, 2, new BigDecimal("99.99"));

            // When
            OrderItem savedItem = orderItemRepository.save(orderItem);

            // Update product price (should not affect order item price)
            testProduct1.setPrice(new BigDecimal("89.99"));
            productRepository.save(testProduct1);

            // Then
            OrderItem foundItem = orderItemRepository.findById(savedItem.getId()).get();
            assertThat(foundItem.getPriceAtPurchase()).isEqualByComparingTo(new BigDecimal("99.99"));
            assertThat(foundItem.getProduct().getPrice()).isEqualByComparingTo(new BigDecimal("89.99"));
        }

        @Test
        @DisplayName("Should maintain bidirectional relationship with order")
        void shouldMaintainBidirectionalRelationshipWithOrder() {
            // Given
            OrderItem orderItem = createOrderItem(testOrder, testProduct1, 2, new BigDecimal("99.99"));

            // When
            OrderItem savedItem = orderItemRepository.save(orderItem);
            testOrder.addOrderItem(savedItem);
            orderRepository.save(testOrder);

            // Then
            Order foundOrder = orderRepository.findById(testOrder.getId()).get();
            assertThat(foundOrder.getOrderItems()).hasSize(1);
            assertThat(foundOrder.getOrderItems()).contains(savedItem);
        }
    }

    @Nested
    @DisplayName("Quantity Operations")
    class QuantityOperations {

        @Test
        @DisplayName("Should save order item with quantity")
        void shouldSaveOrderItemWithQuantity() {
            // Given
            OrderItem orderItem = createOrderItem(testOrder, testProduct1, 5, new BigDecimal("99.99"));

            // When
            OrderItem savedItem = orderItemRepository.save(orderItem);

            // Then
            assertThat(savedItem.getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should update order item quantity")
        void shouldUpdateOrderItemQuantity() {
            // Given
            OrderItem orderItem = createOrderItem(testOrder, testProduct1, 2, new BigDecimal("99.99"));
            OrderItem savedItem = orderItemRepository.save(orderItem);

            // When
            savedItem.setQuantity(5);
            OrderItem updatedItem = orderItemRepository.save(savedItem);

            // Then
            assertThat(updatedItem.getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should handle quantity of 1")
        void shouldHandleQuantityOfOne() {
            // Given
            OrderItem orderItem = createOrderItem(testOrder, testProduct1, 1, new BigDecimal("99.99"));

            // When
            OrderItem savedItem = orderItemRepository.save(orderItem);

            // Then
            assertThat(savedItem.getQuantity()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle large quantities")
        void shouldHandleLargeQuantities() {
            // Given
            OrderItem orderItem = createOrderItem(testOrder, testProduct1, 999, new BigDecimal("99.99"));

            // When
            OrderItem savedItem = orderItemRepository.save(orderItem);

            // Then
            assertThat(savedItem.getQuantity()).isEqualTo(999);
        }
    }

    @Nested
    @DisplayName("Price Operations")
    class PriceOperations {

        @Test
        @DisplayName("Should save order item with price at purchase")
        void shouldSaveOrderItemWithPriceAtPurchase() {
            // Given
            OrderItem orderItem = createOrderItem(testOrder, testProduct1, 2, new BigDecimal("99.99"));

            // When
            OrderItem savedItem = orderItemRepository.save(orderItem);

            // Then
            assertThat(savedItem.getPriceAtPurchase()).isEqualByComparingTo(new BigDecimal("99.99"));
        }

        @Test
        @DisplayName("Should calculate subtotal correctly")
        void shouldCalculateSubtotalCorrectly() {
            // Given
            OrderItem orderItem = createOrderItem(testOrder, testProduct1, 3, new BigDecimal("99.99"));

            // When
            OrderItem savedItem = orderItemRepository.save(orderItem);

            // Then
            BigDecimal expectedSubtotal = new BigDecimal("99.99").multiply(new BigDecimal("3"));
            BigDecimal actualSubtotal = savedItem.getPriceAtPurchase().multiply(new BigDecimal(savedItem.getQuantity()));
            assertThat(actualSubtotal).isEqualByComparingTo(expectedSubtotal);
        }

        @Test
        @DisplayName("Should handle different prices for same product in different orders")
        void shouldHandleDifferentPricesForSameProductInDifferentOrders() {
            // Given
            Order order1 = createOrder(testUser, "ORD-2025-002", OrderStatus.PENDING, new BigDecimal("99.99"));
            Order order2 = createOrder(testUser, "ORD-2025-003", OrderStatus.PENDING, new BigDecimal("79.99"));
            orderRepository.save(order1);
            orderRepository.save(order2);

            OrderItem item1 = createOrderItem(order1, testProduct1, 1, new BigDecimal("99.99"));
            OrderItem item2 = createOrderItem(order2, testProduct1, 1, new BigDecimal("79.99"));

            // When
            OrderItem savedItem1 = orderItemRepository.save(item1);
            OrderItem savedItem2 = orderItemRepository.save(item2);

            // Then
            assertThat(savedItem1.getPriceAtPurchase()).isEqualByComparingTo(new BigDecimal("99.99"));
            assertThat(savedItem2.getPriceAtPurchase()).isEqualByComparingTo(new BigDecimal("79.99"));
        }
    }

    @Nested
    @DisplayName("Update and Delete Operations")
    class UpdateAndDeleteOperations {

        @Test
        @DisplayName("Should update order item")
        void shouldUpdateOrderItem() {
            // Given
            OrderItem orderItem = createOrderItem(testOrder, testProduct1, 2, new BigDecimal("99.99"));
            OrderItem savedItem = orderItemRepository.save(orderItem);

            // When
            savedItem.setQuantity(4);
            OrderItem updatedItem = orderItemRepository.save(savedItem);

            // Then
            assertThat(updatedItem.getQuantity()).isEqualTo(4);
            assertThat(updatedItem.getPriceAtPurchase()).isEqualByComparingTo(new BigDecimal("99.99"));
        }

        @Test
        @DisplayName("Should delete order item by id")
        void shouldDeleteOrderItemById() {
            // Given
            OrderItem orderItem = createOrderItem(testOrder, testProduct1, 2, new BigDecimal("99.99"));
            OrderItem savedItem = orderItemRepository.save(orderItem);
            Long itemId = savedItem.getId();

            // When
            orderItemRepository.deleteById(itemId);

            // Then
            Optional<OrderItem> deletedItem = orderItemRepository.findById(itemId);
            assertThat(deletedItem).isEmpty();
        }

        @Test
        @DisplayName("Should delete all order items")
        void shouldDeleteAllOrderItems() {
            // Given
            orderItemRepository.save(createOrderItem(testOrder, testProduct1, 2, new BigDecimal("99.99")));
            orderItemRepository.save(createOrderItem(testOrder, testProduct2, 1, new BigDecimal("49.99")));

            // When
            orderItemRepository.deleteAll();

            // Then
            List<OrderItem> allItems = orderItemRepository.findAll();
            assertThat(allItems).isEmpty();
        }
    }

    @Nested
    @DisplayName("Subtotal Operations")
    class SubtotalOperations {

        @Test
        @DisplayName("Should automatically calculate subtotal on save")
        void shouldAutomaticallyCalculateSubtotalOnSave() {
            // Given
            OrderItem orderItem = createOrderItem(testOrder, testProduct1, 2, new BigDecimal("99.99"));

            // When
            OrderItem savedItem = orderItemRepository.save(orderItem);

            // Then
            BigDecimal expectedSubtotal = new BigDecimal("99.99").multiply(new BigDecimal("2"));
            assertThat(savedItem.getSubtotal()).isEqualByComparingTo(expectedSubtotal);
        }

        @Test
        @DisplayName("Should recalculate subtotal when quantity changes")
        void shouldRecalculateSubtotalWhenQuantityChanges() {
            // Given
            OrderItem orderItem = createOrderItem(testOrder, testProduct1, 2, new BigDecimal("99.99"));
            OrderItem savedItem = orderItemRepository.save(orderItem);

            // When
            savedItem.setQuantity(5);
            OrderItem updatedItem = orderItemRepository.save(savedItem);

            // Then
            BigDecimal expectedSubtotal = new BigDecimal("99.99").multiply(new BigDecimal("5"));
            assertThat(updatedItem.getSubtotal()).isEqualByComparingTo(expectedSubtotal);
        }
    }

    @Nested
    @DisplayName("Complex Queries and Edge Cases")
    class ComplexQueriesAndEdgeCases {

        @Test
        @DisplayName("Should handle multiple items in same order")
        void shouldHandleMultipleItemsInSameOrder() {
            // Given
            orderItemRepository.save(createOrderItem(testOrder, testProduct1, 2, new BigDecimal("99.99")));
            orderItemRepository.save(createOrderItem(testOrder, testProduct2, 3, new BigDecimal("49.99")));

            // When
            List<OrderItem> allItems = orderItemRepository.findAll();
            List<OrderItem> orderItems = allItems.stream()
                .filter(item -> item.getOrder().getId().equals(testOrder.getId()))
                .toList();

            // Then
            assertThat(orderItems).hasSize(2);
        }

        @Test
        @DisplayName("Should handle same product in different orders")
        void shouldHandleSameProductInDifferentOrders() {
            // Given
            Order order2 = createOrder(testUser, "ORD-2025-002", OrderStatus.PENDING, new BigDecimal("99.99"));
            orderRepository.save(order2);

            OrderItem item1 = createOrderItem(testOrder, testProduct1, 2, new BigDecimal("99.99"));
            OrderItem item2 = createOrderItem(order2, testProduct1, 3, new BigDecimal("99.99"));
            orderItemRepository.save(item1);
            orderItemRepository.save(item2);

            // When
            List<OrderItem> allItems = orderItemRepository.findAll();
            List<OrderItem> product1Items = allItems.stream()
                .filter(item -> item.getProduct().getId().equals(testProduct1.getId()))
                .toList();

            // Then
            assertThat(product1Items).hasSize(2);
            assertThat(product1Items.get(0).getOrder().getId()).isNotEqualTo(product1Items.get(1).getOrder().getId());
        }

        @Test
        @DisplayName("Should handle order item lookup for non-existent id")
        void shouldHandleOrderItemLookupForNonExistentId() {
            // When
            Optional<OrderItem> foundItem = orderItemRepository.findById(999L);

            // Then
            assertThat(foundItem).isEmpty();
        }

        @Test
        @DisplayName("Should maintain order item integrity across order status changes")
        void shouldMaintainOrderItemIntegrityAcrossOrderStatusChanges() {
            // Given
            OrderItem orderItem = createOrderItem(testOrder, testProduct1, 2, new BigDecimal("99.99"));
            OrderItem savedItem = orderItemRepository.save(orderItem);

            // When
            testOrder.setStatus(OrderStatus.PROCESSING);
            orderRepository.save(testOrder);

            testOrder.setStatus(OrderStatus.SHIPPED);
            orderRepository.save(testOrder);

            testOrder.setStatus(OrderStatus.DELIVERED);
            orderRepository.save(testOrder);

            // Then
            OrderItem foundItem = orderItemRepository.findById(savedItem.getId()).get();
            assertThat(foundItem.getQuantity()).isEqualTo(2);
            assertThat(foundItem.getPriceAtPurchase()).isEqualByComparingTo(new BigDecimal("99.99"));
            assertThat(foundItem.getOrder().getStatus()).isEqualTo(OrderStatus.DELIVERED);
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

    private Order createOrder(User user, String orderNumber, OrderStatus status, BigDecimal totalAmount) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(orderNumber);
        order.setStatus(status);
        order.setTotalAmount(totalAmount);
        order.setOrderedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
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

    private OrderItem createOrderItem(Order order, Product product, Integer quantity, BigDecimal priceAtPurchase) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);
        orderItem.setPriceAtPurchase(priceAtPurchase);
        return orderItem;
    }
}
