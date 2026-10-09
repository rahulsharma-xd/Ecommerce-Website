package com.ecommerce.app.repository;

import com.ecommerce.app.entity.Order;
import com.ecommerce.app.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order entity.
 * Provides database access methods for order management.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find all orders for a specific user
     * Ordered by most recent first
     *
     * @param userId the user's ID
     * @return list of orders for the user
     */
    List<Order> findByUserIdOrderByOrderedAtDesc(Long userId);

    /**
     * Find order by unique order number
     *
     * @param orderNumber the order number
     * @return optional order
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find all orders with a specific status
     * Ordered by most recent first
     *
     * @param status the order status
     * @return list of orders with the status
     */
    List<Order> findByStatusOrderByOrderedAtDesc(OrderStatus status);

    /**
     * Find all orders ordered by most recent first
     *
     * @return list of all orders
     */
    List<Order> findAllByOrderByOrderedAtDesc();
}
