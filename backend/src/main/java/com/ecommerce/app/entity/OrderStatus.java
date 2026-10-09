package com.ecommerce.app.entity;

/**
 * Order status enum for tracking order lifecycle.
 * Represents the current state of an order from placement to delivery.
 */
public enum OrderStatus {
    /**
     * Order has been placed but not yet processed
     */
    PENDING,

    /**
     * Order is being prepared for shipment
     */
    PROCESSING,

    /**
     * Order has been shipped and is in transit
     */
    SHIPPED,

    /**
     * Order has been delivered to customer
     */
    DELIVERED,

    /**
     * Order has been cancelled
     */
    CANCELLED
}
