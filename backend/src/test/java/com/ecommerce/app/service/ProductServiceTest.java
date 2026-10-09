package com.ecommerce.app.service;

import com.ecommerce.app.dto.ProductRequest;
import com.ecommerce.app.dto.ProductResponse;
import com.ecommerce.app.entity.Product;
import com.ecommerce.app.exception.InvalidInputException;
import com.ecommerce.app.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService.
 * Tests product management business logic with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private ProductRequest validProductRequest;
    private Product testProduct;
    private Product inactiveProduct;

    @BeforeEach
    void setUp() {
        // Setup valid product request
        validProductRequest = new ProductRequest();
        validProductRequest.setName("Test Product");
        validProductRequest.setDescription("Test Description");
        validProductRequest.setPrice(new BigDecimal("99.99"));
        validProductRequest.setStockQuantity(100);
        validProductRequest.setCategory("Electronics");
        validProductRequest.setBrand("TestBrand");
        validProductRequest.setImageUrl("http://example.com/image.jpg");

        // Setup test product
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQuantity(100);
        testProduct.setCategory("Electronics");
        testProduct.setBrand("TestBrand");
        testProduct.setImageUrl("http://example.com/image.jpg");
        testProduct.setActive(true);
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setUpdatedAt(LocalDateTime.now());

        // Setup inactive product
        inactiveProduct = new Product();
        inactiveProduct.setId(2L);
        inactiveProduct.setName("Inactive Product");
        inactiveProduct.setActive(false);
    }

    @Nested
    @DisplayName("Create Product Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully with valid data")
        void shouldCreateProductSuccessfully() {
            // Given
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            ProductResponse response = productService.createProduct(validProductRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("Test Product");
            assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
            assertThat(response.getStockQuantity()).isEqualTo(100);
            assertThat(response.isActive()).isTrue();

            verify(productRepository).save(argThat(product ->
                product.getName().equals("Test Product") &&
                product.getActive() == true &&
                product.getCreatedAt() != null &&
                product.getUpdatedAt() != null
            ));
        }

        @Test
        @DisplayName("Should throw exception when product name is null")
        void shouldThrowExceptionWhenNameIsNull() {
            // Given
            validProductRequest.setName(null);

            // When/Then
            assertThatThrownBy(() -> productService.createProduct(validProductRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Product name is required");

            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw exception when product name is empty")
        void shouldThrowExceptionWhenNameIsEmpty() {
            // Given
            validProductRequest.setName("   ");

            // When/Then
            assertThatThrownBy(() -> productService.createProduct(validProductRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Product name is required");
        }

        @Test
        @DisplayName("Should throw exception when price is null")
        void shouldThrowExceptionWhenPriceIsNull() {
            // Given
            validProductRequest.setPrice(null);

            // When/Then
            assertThatThrownBy(() -> productService.createProduct(validProductRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Product price is required");
        }

        @Test
        @DisplayName("Should throw exception when price is zero")
        void shouldThrowExceptionWhenPriceIsZero() {
            // Given
            validProductRequest.setPrice(BigDecimal.ZERO);

            // When/Then
            assertThatThrownBy(() -> productService.createProduct(validProductRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Price must be greater than zero");
        }

        @Test
        @DisplayName("Should throw exception when price is negative")
        void shouldThrowExceptionWhenPriceIsNegative() {
            // Given
            validProductRequest.setPrice(new BigDecimal("-10.00"));

            // When/Then
            assertThatThrownBy(() -> productService.createProduct(validProductRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Price must be greater than zero");
        }

        @Test
        @DisplayName("Should throw exception when stock quantity is null")
        void shouldThrowExceptionWhenStockQuantityIsNull() {
            // Given
            validProductRequest.setStockQuantity(null);

            // When/Then
            assertThatThrownBy(() -> productService.createProduct(validProductRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Stock quantity is required");
        }

        @Test
        @DisplayName("Should throw exception when stock quantity is negative")
        void shouldThrowExceptionWhenStockQuantityIsNegative() {
            // Given
            validProductRequest.setStockQuantity(-5);

            // When/Then
            assertThatThrownBy(() -> productService.createProduct(validProductRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Stock quantity cannot be negative");
        }

        @Test
        @DisplayName("Should allow zero stock quantity for create")
        void shouldAllowZeroStockQuantity() {
            // Given
            validProductRequest.setStockQuantity(0);
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When/Then - should not throw exception
            ProductResponse response = productService.createProduct(validProductRequest);
            assertThat(response).isNotNull();
        }
    }

    @Nested
    @DisplayName("Update Product Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product successfully with all fields")
        void shouldUpdateProductSuccessfully() {
            // Given
            ProductRequest updateRequest = new ProductRequest();
            updateRequest.setName("Updated Product");
            updateRequest.setDescription("Updated Description");
            updateRequest.setPrice(new BigDecimal("149.99"));
            updateRequest.setStockQuantity(200);
            updateRequest.setCategory("Updated Category");
            updateRequest.setBrand("Updated Brand");
            updateRequest.setImageUrl("http://example.com/new-image.jpg");

            when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            ProductResponse response = productService.updateProduct(1L, updateRequest);

            // Then
            assertThat(response).isNotNull();
            verify(productRepository).findById(1L);
            verify(productRepository).save(argThat(product ->
                product.getName().equals("Updated Product") &&
                product.getPrice().compareTo(new BigDecimal("149.99")) == 0 &&
                product.getStockQuantity() == 200 &&
                product.getUpdatedAt() != null
            ));
        }

        @Test
        @DisplayName("Should update only provided fields (partial update)")
        void shouldUpdateOnlyProvidedFields() {
            // Given
            ProductRequest partialUpdate = new ProductRequest();
            partialUpdate.setPrice(new BigDecimal("79.99"));
            // Other fields are null - should not be updated

            when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            productService.updateProduct(1L, partialUpdate);

            // Then
            verify(productRepository).save(argThat(product ->
                product.getPrice().compareTo(new BigDecimal("79.99")) == 0 &&
                product.getName().equals("Test Product") // Original name unchanged
            ));
        }

        @Test
        @DisplayName("Should not update name when null or empty")
        void shouldNotUpdateNameWhenNullOrEmpty() {
            // Given
            ProductRequest updateRequest = new ProductRequest();
            updateRequest.setName("   ");
            updateRequest.setPrice(new BigDecimal("50.00"));

            when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            productService.updateProduct(1L, updateRequest);

            // Then
            verify(productRepository).save(argThat(product ->
                product.getName().equals("Test Product") // Name should remain unchanged
            ));
        }

        @Test
        @DisplayName("Should throw exception when updating with zero price")
        void shouldThrowExceptionWhenUpdatingWithZeroPrice() {
            // Given
            ProductRequest updateRequest = new ProductRequest();
            updateRequest.setPrice(BigDecimal.ZERO);

            when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));

            // When/Then
            assertThatThrownBy(() -> productService.updateProduct(1L, updateRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Price must be greater than zero");
        }

        @Test
        @DisplayName("Should throw exception when updating with negative stock")
        void shouldThrowExceptionWhenUpdatingWithNegativeStock() {
            // Given
            ProductRequest updateRequest = new ProductRequest();
            updateRequest.setStockQuantity(-10);

            when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));

            // When/Then
            assertThatThrownBy(() -> productService.updateProduct(1L, updateRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Stock quantity cannot be negative");
        }

        @Test
        @DisplayName("Should throw exception when product not found for update")
        void shouldThrowExceptionWhenProductNotFoundForUpdate() {
            // Given
            when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> productService.updateProduct(999L, validProductRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product not found with id: 999");

            verify(productRepository).findById(999L);
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should update timestamp on product update")
        void shouldUpdateTimestampOnProductUpdate() {
            // Given
            when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            productService.updateProduct(1L, validProductRequest);

            // Then
            verify(productRepository).save(argThat(product ->
                product.getUpdatedAt() != null
            ));
        }
    }

    @Nested
    @DisplayName("Soft Delete Product Tests")
    class SoftDeleteProductTests {

        @Test
        @DisplayName("Should soft delete product successfully")
        void shouldSoftDeleteProductSuccessfully() {
            // Given
            when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            productService.deleteProduct(1L);

            // Then
            verify(productRepository).findById(1L);
            verify(productRepository).save(argThat(product ->
                product.getActive() == false &&
                product.getUpdatedAt() != null
            ));
        }

        @Test
        @DisplayName("Should throw exception when product not found for delete")
        void shouldThrowExceptionWhenProductNotFoundForDelete() {
            // Given
            when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> productService.deleteProduct(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product not found with id: 999");

            verify(productRepository).findById(999L);
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should update timestamp when soft deleting")
        void shouldUpdateTimestampWhenSoftDeleting() {
            // Given
            LocalDateTime beforeDelete = testProduct.getUpdatedAt();
            when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            productService.deleteProduct(1L);

            // Then
            verify(productRepository).save(argThat(product ->
                product.getUpdatedAt() != null
            ));
        }
    }

    @Nested
    @DisplayName("Get Products Tests")
    class GetProductsTests {

        @Test
        @DisplayName("Should get all active products only")
        void shouldGetAllActiveProductsOnly() {
            // Given
            List<Product> products = Arrays.asList(testProduct, inactiveProduct);
            when(productRepository.findByActiveTrue()).thenReturn(Arrays.asList(testProduct));

            // When
            List<ProductResponse> responses = productService.getAllActiveProducts();

            // Then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getId()).isEqualTo(1L);
            assertThat(responses.get(0).isActive()).isTrue();

            verify(productRepository).findByActiveTrue();
        }

        @Test
        @DisplayName("Should get all products including inactive")
        void shouldGetAllProductsIncludingInactive() {
            // Given
            when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct, inactiveProduct));

            // When
            List<ProductResponse> responses = productService.getAllProducts();

            // Then
            assertThat(responses).hasSize(2);
            verify(productRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no active products")
        void shouldReturnEmptyListWhenNoActiveProducts() {
            // Given
            when(productRepository.findByActiveTrue()).thenReturn(Arrays.asList());

            // When
            List<ProductResponse> responses = productService.getAllActiveProducts();

            // Then
            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("Should get product by ID successfully")
        void shouldGetProductByIdSuccessfully() {
            // Given
            when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));

            // When
            ProductResponse response = productService.getProductById(1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("Test Product");

            verify(productRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when product not found by ID")
        void shouldThrowExceptionWhenProductNotFoundById() {
            // Given
            when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product not found with id: 999");

            verify(productRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("Validation Edge Cases")
    class ValidationEdgeCases {

        @Test
        @DisplayName("Should accept minimum valid price (0.01)")
        void shouldAcceptMinimumValidPrice() {
            // Given
            validProductRequest.setPrice(new BigDecimal("0.01"));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When/Then - should not throw exception
            ProductResponse response = productService.createProduct(validProductRequest);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Should accept very large price")
        void shouldAcceptVeryLargePrice() {
            // Given
            validProductRequest.setPrice(new BigDecimal("999999.99"));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When/Then - should not throw exception
            ProductResponse response = productService.createProduct(validProductRequest);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Should accept product name with special characters")
        void shouldAcceptProductNameWithSpecialCharacters() {
            // Given
            validProductRequest.setName("Product (2024) - Special Edition!");
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When/Then - should not throw exception
            ProductResponse response = productService.createProduct(validProductRequest);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Should handle null optional fields gracefully")
        void shouldHandleNullOptionalFieldsGracefully() {
            // Given
            validProductRequest.setDescription(null);
            validProductRequest.setCategory(null);
            validProductRequest.setBrand(null);
            validProductRequest.setImageUrl(null);
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When/Then - should not throw exception
            ProductResponse response = productService.createProduct(validProductRequest);
            assertThat(response).isNotNull();
        }
    }
}
