package com.ecommerce.app.dto;

import com.ecommerce.app.entity.OrderStatus;

/**
 * Request DTO for updating order status.
 * Used by admin to update shipping status.
 */
public class UpdateOrderStatusRequest {

    private OrderStatus status;

    // Constructors
    public UpdateOrderStatusRequest() {
    }

    public UpdateOrderStatusRequest(OrderStatus status) {
        this.status = status;
    }

    // Getters and Setters
    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
