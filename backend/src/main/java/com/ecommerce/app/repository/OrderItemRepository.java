package com.ecommerce.app.repository;

import com.ecommerce.app.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for OrderItem entity.
 * Provides database access methods for order items.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find all order items for a specific order
     *
     * @param orderId the order ID
     * @return list of order items
     */
    List<OrderItem> findByOrderId(Long orderId);
}
