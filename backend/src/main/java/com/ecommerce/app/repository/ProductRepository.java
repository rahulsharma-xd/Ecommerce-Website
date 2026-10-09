package com.ecommerce.app.repository;

import com.ecommerce.app.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find all active products
    List<Product> findByActiveTrue();

    // Find products by category
    List<Product> findByCategory(String category);

    // Find products by brand
    List<Product> findByBrand(String brand);

    // Find active products by category
    List<Product> findByActiveTrueAndCategory(String category);

    // Find active products by brand
    List<Product> findByActiveTrueAndBrand(String brand);

    // Check whether a product already exists
    boolean existsByName(String name);
}
