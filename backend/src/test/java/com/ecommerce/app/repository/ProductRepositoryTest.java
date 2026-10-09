package com.ecommerce.app.repository;

import com.ecommerce.app.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
 * Integration tests for ProductRepository.
 * Uses @DataJpaTest for repository layer testing with in-memory H2 database.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProductRepository Integration Tests")
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        productRepository.deleteAll();
        entityManager.flush();
    }

    @Nested
    @DisplayName("Save and Find Basic Operations")
    class SaveAndFindOperations {

        @Test
        @DisplayName("Should save and find product by id")
        void shouldSaveAndFindProductById() {
            // Given
            Product product = createProduct("Laptop", new BigDecimal("999.99"), 10, "Electronics");

            // When
            Product savedProduct = productRepository.save(product);
            Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());

            // Then
            assertThat(foundProduct).isPresent();
            assertThat(foundProduct.get().getName()).isEqualTo("Laptop");
            assertThat(foundProduct.get().getPrice()).isEqualByComparingTo(new BigDecimal("999.99"));
            assertThat(foundProduct.get().getStockQuantity()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should save product with all fields")
        void shouldSaveProductWithAllFields() {
            // Given
            Product product = new Product();
            product.setName("Gaming Mouse");
            product.setDescription("High precision gaming mouse");
            product.setPrice(new BigDecimal("79.99"));
            product.setStockQuantity(50);
            product.setCategory("Gaming");
            product.setBrand("Logitech");
            product.setImageUrl("http://example.com/mouse.jpg");
            product.setActive(true);
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());

            // When
            Product savedProduct = productRepository.save(product);

            // Then
            assertThat(savedProduct.getId()).isNotNull();
            assertThat(savedProduct.getName()).isEqualTo("Gaming Mouse");
            assertThat(savedProduct.getDescription()).isEqualTo("High precision gaming mouse");
            assertThat(savedProduct.getBrand()).isEqualTo("Logitech");
            assertThat(savedProduct.getCategory()).isEqualTo("Gaming");
        }

        @Test
        @DisplayName("Should find all products")
        void shouldFindAllProducts() {
            // Given
            productRepository.save(createProduct("Product 1", new BigDecimal("10.00"), 5, "Category1"));
            productRepository.save(createProduct("Product 2", new BigDecimal("20.00"), 10, "Category2"));
            productRepository.save(createProduct("Product 3", new BigDecimal("30.00"), 15, "Category3"));

            // When
            List<Product> allProducts = productRepository.findAll();

            // Then
            assertThat(allProducts).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Active Product Filtering")
    class ActiveProductFiltering {

        @Test
        @DisplayName("Should find only active products")
        void shouldFindOnlyActiveProducts() {
            // Given
            Product activeProduct1 = createProduct("Active Product 1", new BigDecimal("50.00"), 10, "Electronics");
            activeProduct1.setActive(true);
            productRepository.save(activeProduct1);

            Product activeProduct2 = createProduct("Active Product 2", new BigDecimal("60.00"), 15, "Electronics");
            activeProduct2.setActive(true);
            productRepository.save(activeProduct2);

            Product inactiveProduct = createProduct("Inactive Product", new BigDecimal("70.00"), 20, "Electronics");
            inactiveProduct.setActive(false);
            productRepository.save(inactiveProduct);

            // When
            List<Product> activeProducts = productRepository.findByActiveTrue();

            // Then
            assertThat(activeProducts).hasSize(2);
            assertThat(activeProducts).allMatch(Product::getActive);
            assertThat(activeProducts).noneMatch(p -> p.getName().equals("Inactive Product"));
        }

        @Test
        @DisplayName("Should return empty list when no active products")
        void shouldReturnEmptyListWhenNoActiveProducts() {
            // Given
            Product inactiveProduct = createProduct("Inactive Product", new BigDecimal("70.00"), 20, "Electronics");
            inactiveProduct.setActive(false);
            productRepository.save(inactiveProduct);

            // When
            List<Product> activeProducts = productRepository.findByActiveTrue();

            // Then
            assertThat(activeProducts).isEmpty();
        }

        @Test
        @DisplayName("Should exclude inactive products from active product list")
        void shouldExcludeInactiveProductsFromActiveProductList() {
            // Given
            productRepository.save(createActiveProduct("Product 1", new BigDecimal("10.00"), 5));
            productRepository.save(createInactiveProduct("Product 2", new BigDecimal("20.00"), 10));
            productRepository.save(createActiveProduct("Product 3", new BigDecimal("30.00"), 15));

            // When
            List<Product> activeProducts = productRepository.findByActiveTrue();

            // Then
            assertThat(activeProducts).hasSize(2);
            assertThat(activeProducts.stream().map(Product::getName))
                .containsExactlyInAnyOrder("Product 1", "Product 3");
        }
    }

    @Nested
    @DisplayName("Stock Management Operations")
    class StockManagementOperations {

        @Test
        @DisplayName("Should save product with stock quantity")
        void shouldSaveProductWithStockQuantity() {
            // Given
            Product product = createProduct("Product", new BigDecimal("50.00"), 100, "Category");

            // When
            Product savedProduct = productRepository.save(product);

            // Then
            assertThat(savedProduct.getStockQuantity()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should update stock quantity")
        void shouldUpdateStockQuantity() {
            // Given
            Product product = createProduct("Product", new BigDecimal("50.00"), 100, "Category");
            Product savedProduct = productRepository.save(product);

            // When
            savedProduct.setStockQuantity(75);
            savedProduct.setUpdatedAt(LocalDateTime.now());
            Product updatedProduct = productRepository.save(savedProduct);

            // Then
            assertThat(updatedProduct.getStockQuantity()).isEqualTo(75);
        }

        @Test
        @DisplayName("Should handle zero stock quantity")
        void shouldHandleZeroStockQuantity() {
            // Given
            Product product = createProduct("Product", new BigDecimal("50.00"), 0, "Category");

            // When
            Product savedProduct = productRepository.save(product);

            // Then
            assertThat(savedProduct.getStockQuantity()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should find products with sufficient stock")
        void shouldFindProductsWithSufficientStock() {
            // Given
            productRepository.save(createProduct("High Stock", new BigDecimal("10.00"), 100, "Category"));
            productRepository.save(createProduct("Low Stock", new BigDecimal("20.00"), 2, "Category"));
            productRepository.save(createProduct("No Stock", new BigDecimal("30.00"), 0, "Category"));

            // When
            List<Product> allProducts = productRepository.findAll();
            List<Product> inStockProducts = allProducts.stream()
                .filter(p -> p.getStockQuantity() > 0)
                .toList();

            // Then
            assertThat(inStockProducts).hasSize(2);
            assertThat(inStockProducts.stream().map(Product::getName))
                .containsExactlyInAnyOrder("High Stock", "Low Stock");
        }
    }

    @Nested
    @DisplayName("Update and Delete Operations")
    class UpdateAndDeleteOperations {

        @Test
        @DisplayName("Should update product name")
        void shouldUpdateProductName() {
            // Given
            Product product = createProduct("Old Name", new BigDecimal("50.00"), 10, "Category");
            Product savedProduct = productRepository.save(product);

            // When
            savedProduct.setName("New Name");
            savedProduct.setUpdatedAt(LocalDateTime.now());
            Product updatedProduct = productRepository.save(savedProduct);

            // Then
            assertThat(updatedProduct.getName()).isEqualTo("New Name");
        }

        @Test
        @DisplayName("Should update product price")
        void shouldUpdateProductPrice() {
            // Given
            Product product = createProduct("Product", new BigDecimal("50.00"), 10, "Category");
            Product savedProduct = productRepository.save(product);

            // When
            savedProduct.setPrice(new BigDecimal("75.00"));
            savedProduct.setUpdatedAt(LocalDateTime.now());
            Product updatedProduct = productRepository.save(savedProduct);

            // Then
            assertThat(updatedProduct.getPrice()).isEqualByComparingTo(new BigDecimal("75.00"));
        }

        @Test
        @DisplayName("Should soft delete product by setting active to false")
        void shouldSoftDeleteProduct() {
            // Given
            Product product = createProduct("Product", new BigDecimal("50.00"), 10, "Category");
            product.setActive(true);
            Product savedProduct = productRepository.save(product);

            // When
            savedProduct.setActive(false);
            savedProduct.setUpdatedAt(LocalDateTime.now());
            Product deletedProduct = productRepository.save(savedProduct);

            // Then
            assertThat(deletedProduct.getActive()).isFalse();

            // Verify product still exists in database
            Optional<Product> foundProduct = productRepository.findById(deletedProduct.getId());
            assertThat(foundProduct).isPresent();
            assertThat(foundProduct.get().getActive()).isFalse();
        }

        @Test
        @DisplayName("Should hard delete product by id")
        void shouldHardDeleteProductById() {
            // Given
            Product product = createProduct("Product", new BigDecimal("50.00"), 10, "Category");
            Product savedProduct = productRepository.save(product);
            Long productId = savedProduct.getId();

            // When
            productRepository.deleteById(productId);

            // Then
            Optional<Product> deletedProduct = productRepository.findById(productId);
            assertThat(deletedProduct).isEmpty();
        }

        @Test
        @DisplayName("Should delete all products")
        void shouldDeleteAllProducts() {
            // Given
            productRepository.save(createProduct("Product 1", new BigDecimal("10.00"), 5, "Category1"));
            productRepository.save(createProduct("Product 2", new BigDecimal("20.00"), 10, "Category2"));
            productRepository.save(createProduct("Product 3", new BigDecimal("30.00"), 15, "Category3"));

            // When
            productRepository.deleteAll();

            // Then
            List<Product> allProducts = productRepository.findAll();
            assertThat(allProducts).isEmpty();
        }
    }

    @Nested
    @DisplayName("Category and Brand Operations")
    class CategoryAndBrandOperations {

        @Test
        @DisplayName("Should save and retrieve product category")
        void shouldSaveAndRetrieveProductCategory() {
            // Given
            Product product = createProduct("Laptop", new BigDecimal("999.99"), 10, "Electronics");

            // When
            Product savedProduct = productRepository.save(product);

            // Then
            assertThat(savedProduct.getCategory()).isEqualTo("Electronics");
        }

        @Test
        @DisplayName("Should save and retrieve product brand")
        void shouldSaveAndRetrieveProductBrand() {
            // Given
            Product product = createProduct("Laptop", new BigDecimal("999.99"), 10, "Electronics");
            product.setBrand("Dell");

            // When
            Product savedProduct = productRepository.save(product);

            // Then
            assertThat(savedProduct.getBrand()).isEqualTo("Dell");
        }

        @Test
        @DisplayName("Should find products by category")
        void shouldFindProductsByCategory() {
            // Given
            productRepository.save(createProduct("Laptop", new BigDecimal("999.99"), 10, "Electronics"));
            productRepository.save(createProduct("Phone", new BigDecimal("699.99"), 20, "Electronics"));
            productRepository.save(createProduct("Shirt", new BigDecimal("29.99"), 50, "Clothing"));

            // When
            List<Product> allProducts = productRepository.findAll();
            List<Product> electronicsProducts = allProducts.stream()
                .filter(p -> "Electronics".equals(p.getCategory()))
                .toList();

            // Then
            assertThat(electronicsProducts).hasSize(2);
            assertThat(electronicsProducts.stream().map(Product::getName))
                .containsExactlyInAnyOrder("Laptop", "Phone");
        }
    }

    @Nested
    @DisplayName("Timestamp Operations")
    class TimestampOperations {

        @Test
        @DisplayName("Should save product with timestamps")
        void shouldSaveProductWithTimestamps() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Product product = createProduct("Product", new BigDecimal("50.00"), 10, "Category");
            product.setCreatedAt(now);
            product.setUpdatedAt(now);

            // When
            Product savedProduct = productRepository.save(product);

            // Then
            assertThat(savedProduct.getCreatedAt()).isNotNull();
            assertThat(savedProduct.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should update updatedAt timestamp on modification")
        void shouldUpdateUpdatedAtTimestamp() throws InterruptedException {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Product product = createProduct("Product", new BigDecimal("50.00"), 10, "Category");
            product.setCreatedAt(now);
            product.setUpdatedAt(now);
            Product savedProduct = productRepository.save(product);

            // Wait a moment to ensure different timestamp
            Thread.sleep(10);

            // When
            LocalDateTime newTime = LocalDateTime.now();
            savedProduct.setPrice(new BigDecimal("75.00"));
            savedProduct.setUpdatedAt(newTime);
            Product updatedProduct = productRepository.save(savedProduct);

            // Then
            assertThat(updatedProduct.getCreatedAt()).isEqualTo(now);
            assertThat(updatedProduct.getUpdatedAt()).isAfter(savedProduct.getCreatedAt());
        }
    }

    // Helper methods
    private Product createProduct(String name, BigDecimal price, Integer stockQuantity, String category) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("Description for " + name);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setCategory(category);
        product.setBrand("Generic Brand");
        product.setImageUrl("http://example.com/image.jpg");
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    private Product createActiveProduct(String name, BigDecimal price, Integer stockQuantity) {
        Product product = createProduct(name, price, stockQuantity, "Category");
        product.setActive(true);
        return product;
    }

    private Product createInactiveProduct(String name, BigDecimal price, Integer stockQuantity) {
        Product product = createProduct(name, price, stockQuantity, "Category");
        product.setActive(false);
        return product;
    }
}
