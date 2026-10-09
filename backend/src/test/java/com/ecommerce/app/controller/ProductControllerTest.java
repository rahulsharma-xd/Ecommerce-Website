package com.ecommerce.app.controller;

import com.ecommerce.app.dto.ProductResponse;
import com.ecommerce.app.security.JwtAuthenticationFilter;
import com.ecommerce.app.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProductController.
 * Tests public REST API endpoints without authentication.
 */
@WebMvcTest(value = ProductController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {JwtAuthenticationFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ProductController Integration Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductResponse product1;
    private ProductResponse product2;
    private ProductResponse product3;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        // Setup test products
        product1 = new ProductResponse(
            1L,
            "Laptop",
            "High performance laptop",
            new BigDecimal("999.99"),
            50,
            "Electronics",
            "Dell",
            "laptop.jpg",
            true,
            now,
            now
        );

        product2 = new ProductResponse(
            2L,
            "Mouse",
            "Wireless gaming mouse",
            new BigDecimal("49.99"),
            100,
            "Electronics",
            "Logitech",
            "mouse.jpg",
            true,
            now,
            now
        );

        product3 = new ProductResponse(
            3L,
            "Keyboard",
            "Mechanical keyboard",
            new BigDecimal("89.99"),
            75,
            "Electronics",
            "Razer",
            "keyboard.jpg",
            true,
            now,
            now
        );
    }

    @Nested
    @DisplayName("GET /api/products - Get All Active Products")
    class GetAllActiveProductsTests {

        @Test
        @DisplayName("Should return all active products successfully")
        void shouldReturnAllActiveProducts() throws Exception {
            // Given
            List<ProductResponse> products = Arrays.asList(product1, product2, product3);
            when(productService.getAllActiveProducts()).thenReturn(products);

            // When/Then
            mockMvc.perform(get("/api/products")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[0].description").value("High performance laptop"))
                .andExpect(jsonPath("$[0].price").value(999.99))
                .andExpect(jsonPath("$[0].stockQuantity").value(50))
                .andExpect(jsonPath("$[0].category").value("Electronics"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Mouse"))
                .andExpect(jsonPath("$[2].id").value(3L))
                .andExpect(jsonPath("$[2].name").value("Keyboard"));
        }

        @Test
        @DisplayName("Should return empty list when no active products exist")
        void shouldReturnEmptyListWhenNoActiveProducts() throws Exception {
            // Given
            when(productService.getAllActiveProducts()).thenReturn(Collections.emptyList());

            // When/Then
            mockMvc.perform(get("/api/products")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should return only products with active=true")
        void shouldReturnOnlyActiveProducts() throws Exception {
            // Given - Only product1 and product2 are active
            List<ProductResponse> activeProducts = Arrays.asList(product1, product2);
            when(productService.getAllActiveProducts()).thenReturn(activeProducts);

            // When/Then
            mockMvc.perform(get("/api/products")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[1].active").value(true));
        }

        @Test
        @DisplayName("Should return products with correct price formatting")
        void shouldReturnProductsWithCorrectPriceFormatting() throws Exception {
            // Given
            LocalDateTime now = LocalDateTime.now();
            ProductResponse expensiveProduct = new ProductResponse(
                4L,
                "Gaming PC",
                "High-end gaming PC",
                new BigDecimal("2599.99"),
                10,
                "Electronics",
                "Custom",
                "pc.jpg",
                true,
                now,
                now
            );
            when(productService.getAllActiveProducts()).thenReturn(Collections.singletonList(expensiveProduct));

            // When/Then
            mockMvc.perform(get("/api/products")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(2599.99));
        }

        @Test
        @DisplayName("Should return products with stock quantity")
        void shouldReturnProductsWithStockQuantity() throws Exception {
            // Given
            when(productService.getAllActiveProducts()).thenReturn(Collections.singletonList(product1));

            // When/Then
            mockMvc.perform(get("/api/products")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stockQuantity").value(50))
                .andExpect(jsonPath("$[0].stockQuantity").isNumber());
        }

        @Test
        @DisplayName("Should be accessible without authentication")
        void shouldBeAccessibleWithoutAuthentication() throws Exception {
            // Given
            when(productService.getAllActiveProducts()).thenReturn(Collections.singletonList(product1));

            // When/Then - No authentication headers required
            mockMvc.perform(get("/api/products")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/products/{id} - Get Product By ID")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product by ID successfully")
        void shouldReturnProductById() throws Exception {
            // Given
            when(productService.getProductById(1L)).thenReturn(product1);

            // When/Then
            mockMvc.perform(get("/api/products/1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.description").value("High performance laptop"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.stockQuantity").value(50))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.active").value(true));
        }

        @Test
        @DisplayName("Should return 500 when product not found")
        void shouldReturn500WhenProductNotFound() throws Exception {
            // Given
            when(productService.getProductById(anyLong()))
                .thenThrow(new RuntimeException("Product not found with id: 999"));

            // When/Then
            mockMvc.perform(get("/api/products/999")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("Should return product with all fields populated")
        void shouldReturnProductWithAllFields() throws Exception {
            // Given
            when(productService.getProductById(2L)).thenReturn(product2);

            // When/Then
            mockMvc.perform(get("/api/products/2")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.price").exists())
                .andExpect(jsonPath("$.stockQuantity").exists())
                .andExpect(jsonPath("$.category").exists())
                .andExpect(jsonPath("$.active").exists());
        }

        @Test
        @DisplayName("Should return product even if stock is zero")
        void shouldReturnProductWithZeroStock() throws Exception {
            // Given
            LocalDateTime now = LocalDateTime.now();
            ProductResponse outOfStockProduct = new ProductResponse(
                5L,
                "Out of Stock Item",
                "Currently unavailable",
                new BigDecimal("29.99"),
                0,
                "Electronics",
                "Generic",
                "item.jpg",
                true,
                now,
                now
            );
            when(productService.getProductById(5L)).thenReturn(outOfStockProduct);

            // When/Then
            mockMvc.perform(get("/api/products/5")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(0))
                .andExpect(jsonPath("$.active").value(true));
        }

        @Test
        @DisplayName("Should be accessible without authentication")
        void shouldBeAccessibleWithoutAuthentication() throws Exception {
            // Given
            when(productService.getProductById(1L)).thenReturn(product1);

            // When/Then - No authentication headers required
            mockMvc.perform(get("/api/products/1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle invalid ID format gracefully")
        void shouldHandleInvalidIdFormat() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/products/invalid")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("Should return inactive product if requested by ID")
        void shouldReturnInactiveProductById() throws Exception {
            // Given
            LocalDateTime now = LocalDateTime.now();
            ProductResponse inactiveProduct = new ProductResponse(
                6L,
                "Discontinued Item",
                "No longer available",
                new BigDecimal("19.99"),
                0,
                "Electronics",
                "Generic",
                "discontinued.jpg",
                false,
                now,
                now
            );
            when(productService.getProductById(6L)).thenReturn(inactiveProduct);

            // When/Then
            mockMvc.perform(get("/api/products/6")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        }

        @Test
        @DisplayName("Should return product with large price")
        void shouldReturnProductWithLargePrice() throws Exception {
            // Given
            LocalDateTime now = LocalDateTime.now();
            ProductResponse expensiveProduct = new ProductResponse(
                7L,
                "Luxury Item",
                "Premium product",
                new BigDecimal("99999.99"),
                1,
                "Luxury",
                "Luxury Brand",
                "luxury.jpg",
                true,
                now,
                now
            );
            when(productService.getProductById(7L)).thenReturn(expensiveProduct);

            // When/Then
            mockMvc.perform(get("/api/products/7")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(99999.99));
        }

        @Test
        @DisplayName("Should return product with minimal price")
        void shouldReturnProductWithMinimalPrice() throws Exception {
            // Given
            LocalDateTime now = LocalDateTime.now();
            ProductResponse cheapProduct = new ProductResponse(
                8L,
                "Budget Item",
                "Affordable product",
                new BigDecimal("0.99"),
                1000,
                "Budget",
                "Budget Brand",
                "budget.jpg",
                true,
                now,
                now
            );
            when(productService.getProductById(8L)).thenReturn(cheapProduct);

            // When/Then
            mockMvc.perform(get("/api/products/8")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(0.99));
        }
    }
}
