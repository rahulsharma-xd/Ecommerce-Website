package com.ecommerce.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String category;
    private String brand;
    private String imageUrl;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductResponse(Long id, String name, String description, BigDecimal price,
                          Integer stockQuantity, String category, String brand,
                          String imageUrl, boolean active, LocalDateTime createdAt,
                          LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
        this.brand = brand;
        this.imageUrl = imageUrl;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public String getCategory() { return category; }
    public String getBrand() { return brand; }
    public String getImageUrl() { return imageUrl; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
