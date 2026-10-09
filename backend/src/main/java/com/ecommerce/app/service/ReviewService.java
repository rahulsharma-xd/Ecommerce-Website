package com.ecommerce.app.service;

import com.ecommerce.app.dto.ReviewDTO;
import com.ecommerce.app.entity.Product;
import com.ecommerce.app.entity.Review;
import com.ecommerce.app.entity.User;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.repository.ReviewRepository;
import com.ecommerce.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // Add review
    public ReviewDTO addReview(
            Long productId,
            String username,
            Integer rating,
            String comment
    ) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (reviewRepository.existsByProductAndUser(product, user)) {
            throw new RuntimeException(
                    "You have already reviewed this product."
            );
        }

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        return convertToDTO(savedReview);
    }

    // Get reviews by product
    public List<ReviewDTO> getReviewsByProduct(Long productId) {

        return reviewRepository.findByProductId(productId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Convert entity -> DTO
    private ReviewDTO convertToDTO(Review review) {

        ReviewDTO dto = new ReviewDTO();

        dto.setId(review.getId());
        dto.setProductId(review.getProduct().getId());
        dto.setUsername(review.getUser().getUsername());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());

        return dto;
    }
}