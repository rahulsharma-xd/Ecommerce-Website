package com.ecommerce.app.repository;

import com.ecommerce.app.entity.Review;
import com.ecommerce.app.entity.Product;
import com.ecommerce.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Get all reviews for a product
    List<Review> findByProduct(Product product);

    // Get all reviews for a product by product id
    List<Review> findByProductId(Long productId);

    // Check whether a user has already reviewed a product
    boolean existsByProductAndUser(Product product, User user);

    // Get a user's review for a product
    Review findByProductAndUser(Product product, User user);

    // Count reviews of a product
    long countByProduct(Product product);
}