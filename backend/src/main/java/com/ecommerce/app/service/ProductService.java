package com.ecommerce.app.service;

import com.ecommerce.app.dto.ProductRequest;
import com.ecommerce.app.dto.ProductResponse;
import com.ecommerce.app.entity.Product;
import com.ecommerce.app.exception.InvalidInputException;
import com.ecommerce.app.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * Get all active products (public endpoint)
     * Returns only products that are marked as active
     */
    public List<ProductResponse> getAllActiveProducts() {
        return productRepository.findByActiveTrue()
            .stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get all products including inactive ones (admin endpoint)
     * Returns all products in the database
     */
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
            .stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get product by ID
     * Throws exception if product not found
     */
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        return convertToResponse(product);
    }

    /**
     * Create new product (admin endpoint)
     * Validates input and sets timestamps
     */
    public ProductResponse createProduct(ProductRequest request) {
        // Validate input
        validateProductRequest(request);

        // Create new product entity
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(request.getCategory());
        product.setBrand(request.getBrand());
        product.setImageUrl(request.getImageUrl());
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        // Save to database
        Product savedProduct = productRepository.save(product);

        return convertToResponse(savedProduct);
    }

    /**
     * Update existing product (admin endpoint)
     * Supports partial updates - only updates non-null fields
     */
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        // Find existing product
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Update fields if provided (partial update support)
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            if (request.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new InvalidInputException("Price must be greater than zero");
            }
            product.setPrice(request.getPrice());
        }
        if (request.getStockQuantity() != null) {
            if (request.getStockQuantity() < 0) {
                throw new InvalidInputException("Stock quantity cannot be negative");
            }
            product.setStockQuantity(request.getStockQuantity());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getBrand() != null) {
            product.setBrand(request.getBrand());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }

        // Update timestamp
        product.setUpdatedAt(LocalDateTime.now());

        // Save to database
        Product updatedProduct = productRepository.save(product);

        return convertToResponse(updatedProduct);
    }

    /**
     * Soft delete product (admin endpoint)
     * Sets active flag to false instead of deleting from database
     */
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Soft delete - mark as inactive
        product.setActive(false);
        product.setUpdatedAt(LocalDateTime.now());

        productRepository.save(product);
    }

    /**
     * Validate product request input
     */
    private void validateProductRequest(ProductRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidInputException("Product name is required");
        }
        if (request.getPrice() == null) {
            throw new InvalidInputException("Product price is required");
        }
        if (request.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new InvalidInputException("Price must be greater than zero");
        }
        if (request.getStockQuantity() == null) {
            throw new InvalidInputException("Stock quantity is required");
        }
        if (request.getStockQuantity() < 0) {
            throw new InvalidInputException("Stock quantity cannot be negative");
        }
    }

    /**
     * Convert Product entity to ProductResponse DTO using Stream API
     */
    private ProductResponse convertToResponse(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStockQuantity(),
            product.getCategory(),
            product.getBrand(),
            product.getImageUrl(),
            product.getActive(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }
}
