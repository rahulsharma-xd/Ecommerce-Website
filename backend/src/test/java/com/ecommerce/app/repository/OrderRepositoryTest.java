package com.ecommerce.app.repository;

import com.ecommerce.app.entity.Order;
import com.ecommerce.app.entity.OrderStatus;
import com.ecommerce.app.entity.Role;
import com.ecommerce.app.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
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
 * Integration tests for OrderRepository.
 * Uses @DataJpaTest for repository layer testing with in-memory H2 database.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("OrderRepository Integration Tests")
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        // Clean up database
        orderRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setRole(Role.USER);
        testUser.setActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser = entityManager.persistAndFlush(testUser);

        anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password123");
        anotherUser.setRole(Role.USER);
        anotherUser.setActive(true);
        anotherUser.setCreatedAt(LocalDateTime.now());
        anotherUser.setUpdatedAt(LocalDateTime.now());
        anotherUser = entityManager.persistAndFlush(anotherUser);
    }

    @Nested
    @DisplayName("Save and Find Basic Operations")
    class SaveAndFindOperations {

        @Test
        @DisplayName("Should save and find order by id")
        void shouldSaveAndFindOrderById() {
            // Given
            Order order = createOrder(testUser, "ORD-2025-001", OrderStatus.PENDING, new BigDecimal("100.00"));

            // When
            Order savedOrder = orderRepository.save(order);
            Optional<Order> foundOrder = orderRepository.findById(savedOrder.getId());

            // Then
            assertThat(foundOrder).isPresent();
            assertThat(foundOrder.get().getOrderNumber()).isEqualTo("ORD-2025-001");
            assertThat(foundOrder.get().getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(foundOrder.get().getTotalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("Should find order by unique order number")
        void shouldFindOrderByUniqueOrderNumber() {
            // Given
            Order order = createOrder(testUser, "ORD-2025-UNIQUE", OrderStatus.PENDING, new BigDecimal("200.00"));
            orderRepository.save(order);

            // When
            Optional<Order> foundOrder = orderRepository.findByOrderNumber("ORD-2025-UNIQUE");

            // Then
            assertThat(foundOrder).isPresent();
            assertThat(foundOrder.get().getOrderNumber()).isEqualTo("ORD-2025-UNIQUE");
            assertThat(foundOrder.get().getUser().getId()).isEqualTo(testUser.getId());
        }

        @Test
        @DisplayName("Should return empty when order number not found")
        void shouldReturnEmptyWhenOrderNumberNotFound() {
            // When
            Optional<Order> foundOrder = orderRepository.findByOrderNumber("NON-EXISTENT");

            // Then
            assertThat(foundOrder).isEmpty();
        }

        @Test
        @DisplayName("Should save order with all required fields")
        void shouldSaveOrderWithAllRequiredFields() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Order order = new Order();
            order.setUser(testUser);
            order.setOrderNumber("ORD-2025-COMPLETE");
            order.setStatus(OrderStatus.PROCESSING);
            order.setTotalAmount(new BigDecimal("500.00"));
            order.setOrderedAt(now);
            order.setUpdatedAt(now);

            // When
            Order savedOrder = orderRepository.save(order);

            // Then
            assertThat(savedOrder.getId()).isNotNull();
            assertThat(savedOrder.getUser()).isEqualTo(testUser);
            assertThat(savedOrder.getOrderNumber()).isEqualTo("ORD-2025-COMPLETE");
            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PROCESSING);
            assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
            assertThat(savedOrder.getOrderedAt()).isEqualTo(now);
            assertThat(savedOrder.getUpdatedAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("Find By User Operations")
    class FindByUserOperations {

        @Test
        @DisplayName("Should find all orders for a specific user")
        void shouldFindAllOrdersForSpecificUser() {
            // Given
            createAndSaveOrder(testUser, "ORD-001", OrderStatus.PENDING, "100.00");
            createAndSaveOrder(testUser, "ORD-002", OrderStatus.PROCESSING, "200.00");
            createAndSaveOrder(anotherUser, "ORD-003", OrderStatus.SHIPPED, "300.00");

            // When
            List<Order> userOrders = orderRepository.findByUserIdOrderByOrderedAtDesc(testUser.getId());

            // Then
            assertThat(userOrders).hasSize(2);
            assertThat(userOrders).allMatch(order -> order.getUser().getId().equals(testUser.getId()));
        }

        @Test
        @DisplayName("Should return orders in descending order by orderedAt")
        void shouldReturnOrdersInDescendingOrderByOrderedAt() throws InterruptedException {
            // Given
            Order order1 = createOrder(testUser, "ORD-001", OrderStatus.PENDING, new BigDecimal("100.00"));
            order1.setOrderedAt(LocalDateTime.now().minusDays(2));
            orderRepository.save(order1);

            Thread.sleep(10); // Ensure different timestamps

            Order order2 = createOrder(testUser, "ORD-002", OrderStatus.PROCESSING, new BigDecimal("200.00"));
            order2.setOrderedAt(LocalDateTime.now().minusDays(1));
            orderRepository.save(order2);

            Thread.sleep(10);

            Order order3 = createOrder(testUser, "ORD-003", OrderStatus.SHIPPED, new BigDecimal("300.00"));
            order3.setOrderedAt(LocalDateTime.now());
            orderRepository.save(order3);

            // When
            List<Order> userOrders = orderRepository.findByUserIdOrderByOrderedAtDesc(testUser.getId());

            // Then
            assertThat(userOrders).hasSize(3);
            assertThat(userOrders.get(0).getOrderNumber()).isEqualTo("ORD-003"); // Most recent
            assertThat(userOrders.get(1).getOrderNumber()).isEqualTo("ORD-002");
            assertThat(userOrders.get(2).getOrderNumber()).isEqualTo("ORD-001"); // Oldest
        }

        @Test
        @DisplayName("Should return empty list when user has no orders")
        void shouldReturnEmptyListWhenUserHasNoOrders() {
            // When
            List<Order> userOrders = orderRepository.findByUserIdOrderByOrderedAtDesc(testUser.getId());

            // Then
            assertThat(userOrders).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find By Status Operations")
    class FindByStatusOperations {

        @Test
        @DisplayName("Should find all orders with PENDING status")
        void shouldFindAllOrdersWithPendingStatus() {
            // Given
            createAndSaveOrder(testUser, "ORD-001", OrderStatus.PENDING, "100.00");
            createAndSaveOrder(testUser, "ORD-002", OrderStatus.PENDING, "200.00");
            createAndSaveOrder(anotherUser, "ORD-003", OrderStatus.PROCESSING, "300.00");

            // When
            List<Order> pendingOrders = orderRepository.findByStatusOrderByOrderedAtDesc(OrderStatus.PENDING);

            // Then
            assertThat(pendingOrders).hasSize(2);
            assertThat(pendingOrders).allMatch(order -> order.getStatus() == OrderStatus.PENDING);
        }

        @Test
        @DisplayName("Should find orders for each status type")
        void shouldFindOrdersForEachStatusType() {
            // Given
            createAndSaveOrder(testUser, "ORD-001", OrderStatus.PENDING, "100.00");
            createAndSaveOrder(testUser, "ORD-002", OrderStatus.PROCESSING, "200.00");
            createAndSaveOrder(testUser, "ORD-003", OrderStatus.SHIPPED, "300.00");
            createAndSaveOrder(testUser, "ORD-004", OrderStatus.DELIVERED, "400.00");
            createAndSaveOrder(testUser, "ORD-005", OrderStatus.CANCELLED, "500.00");

            // When & Then
            assertThat(orderRepository.findByStatusOrderByOrderedAtDesc(OrderStatus.PENDING)).hasSize(1);
            assertThat(orderRepository.findByStatusOrderByOrderedAtDesc(OrderStatus.PROCESSING)).hasSize(1);
            assertThat(orderRepository.findByStatusOrderByOrderedAtDesc(OrderStatus.SHIPPED)).hasSize(1);
            assertThat(orderRepository.findByStatusOrderByOrderedAtDesc(OrderStatus.DELIVERED)).hasSize(1);
            assertThat(orderRepository.findByStatusOrderByOrderedAtDesc(OrderStatus.CANCELLED)).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty list when no orders have the specified status")
        void shouldReturnEmptyListWhenNoOrdersHaveSpecifiedStatus() {
            // Given
            createAndSaveOrder(testUser, "ORD-001", OrderStatus.PENDING, "100.00");

            // When
            List<Order> deliveredOrders = orderRepository.findByStatusOrderByOrderedAtDesc(OrderStatus.DELIVERED);

            // Then
            assertThat(deliveredOrders).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find All Operations")
    class FindAllOperations {

        @Test
        @DisplayName("Should find all orders ordered by date descending")
        void shouldFindAllOrdersOrderedByDateDescending() throws InterruptedException {
            // Given
            Order order1 = createOrder(testUser, "ORD-001", OrderStatus.PENDING, new BigDecimal("100.00"));
            order1.setOrderedAt(LocalDateTime.now().minusDays(3));
            orderRepository.save(order1);

            Thread.sleep(10);

            Order order2 = createOrder(anotherUser, "ORD-002", OrderStatus.PROCESSING, new BigDecimal("200.00"));
            order2.setOrderedAt(LocalDateTime.now().minusDays(1));
            orderRepository.save(order2);

            Thread.sleep(10);

            Order order3 = createOrder(testUser, "ORD-003", OrderStatus.SHIPPED, new BigDecimal("300.00"));
            order3.setOrderedAt(LocalDateTime.now());
            orderRepository.save(order3);

            // When
            List<Order> allOrders = orderRepository.findAllByOrderByOrderedAtDesc();

            // Then
            assertThat(allOrders).hasSize(3);
            assertThat(allOrders.get(0).getOrderNumber()).isEqualTo("ORD-003"); // Most recent
            assertThat(allOrders.get(1).getOrderNumber()).isEqualTo("ORD-002");
            assertThat(allOrders.get(2).getOrderNumber()).isEqualTo("ORD-001"); // Oldest
        }

        @Test
        @DisplayName("Should return empty list when no orders exist")
        void shouldReturnEmptyListWhenNoOrdersExist() {
            // When
            List<Order> allOrders = orderRepository.findAllByOrderByOrderedAtDesc();

            // Then
            assertThat(allOrders).isEmpty();
        }
    }

    @Nested
    @DisplayName("Update Operations")
    class UpdateOperations {

        @Test
        @DisplayName("Should update order status")
        void shouldUpdateOrderStatus() {
            // Given
            Order order = createAndSaveOrder(testUser, "ORD-001", OrderStatus.PENDING, "100.00");

            // When
            order.setStatus(OrderStatus.PROCESSING);
            order.setUpdatedAt(LocalDateTime.now());
            Order updatedOrder = orderRepository.save(order);

            // Then
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PROCESSING);
            assertThat(updatedOrder.getOrderNumber()).isEqualTo("ORD-001");
        }

        @Test
        @DisplayName("Should update order total amount")
        void shouldUpdateOrderTotalAmount() {
            // Given
            Order order = createAndSaveOrder(testUser, "ORD-001", OrderStatus.PENDING, "100.00");

            // When
            order.setTotalAmount(new BigDecimal("250.00"));
            order.setUpdatedAt(LocalDateTime.now());
            Order updatedOrder = orderRepository.save(order);

            // Then
            assertThat(updatedOrder.getTotalAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {

        @Test
        @DisplayName("Should delete order by id")
        void shouldDeleteOrderById() {
            // Given
            Order order = createAndSaveOrder(testUser, "ORD-001", OrderStatus.PENDING, "100.00");
            Long orderId = order.getId();

            // When
            orderRepository.deleteById(orderId);

            // Then
            Optional<Order> deletedOrder = orderRepository.findById(orderId);
            assertThat(deletedOrder).isEmpty();
        }

        @Test
        @DisplayName("Should delete all orders")
        void shouldDeleteAllOrders() {
            // Given
            createAndSaveOrder(testUser, "ORD-001", OrderStatus.PENDING, "100.00");
            createAndSaveOrder(testUser, "ORD-002", OrderStatus.PROCESSING, "200.00");
            createAndSaveOrder(anotherUser, "ORD-003", OrderStatus.SHIPPED, "300.00");

            // When
            orderRepository.deleteAll();

            // Then
            List<Order> allOrders = orderRepository.findAll();
            assertThat(allOrders).isEmpty();
        }
    }

    // Helper methods
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

    private Order createAndSaveOrder(User user, String orderNumber, OrderStatus status, String totalAmount) {
        Order order = createOrder(user, orderNumber, status, new BigDecimal(totalAmount));
        return orderRepository.save(order);
    }
}
