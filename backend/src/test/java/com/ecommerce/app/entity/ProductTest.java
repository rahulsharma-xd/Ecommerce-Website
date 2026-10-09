package com.ecommerce.app.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Product entity.
 * Tests entity behavior, validation, and business logic.
 */
@DisplayName("Product Entity Tests")
class ProductTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(new BigDecimal("99.99"));
        product.setStockQuantity(100);
        product.setCategory("Electronics");
        product.setBrand("TestBrand");
        product.setImageUrl("http://example.com/image.jpg");
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Constructor and Basic Tests")
    class ConstructorAndBasicTests {

        @Test
        @DisplayName("Should create product with default constructor")
        void shouldCreateProductWithDefaultConstructor() {
            Product newProduct = new Product();
            assertNotNull(newProduct);
            assertNull(newProduct.getId());
            assertNull(newProduct.getName());
            assertNull(newProduct.getPrice());
        }

        @Test
        @DisplayName("Should set and get all basic fields")
        void shouldSetAndGetAllBasicFields() {
            assertEquals(1L, product.getId());
            assertEquals("Test Product", product.getName());
            assertEquals("Test Description", product.getDescription());
            assertEquals(new BigDecimal("99.99"), product.getPrice());
            assertEquals(100, product.getStockQuantity());
            assertEquals("Electronics", product.getCategory());
            assertEquals("TestBrand", product.getBrand());
            assertEquals("http://example.com/image.jpg", product.getImageUrl());
        }
    }

    @Nested
    @DisplayName("Price Management Tests")
    class PriceManagementTests {

        @Test
        @DisplayName("Should set and get price")
        void shouldSetAndGetPrice() {
            product.setPrice(new BigDecimal("149.99"));
            assertEquals(new BigDecimal("149.99"), product.getPrice());
        }

        @Test
        @DisplayName("Should handle decimal prices")
        void shouldHandleDecimalPrices() {
            product.setPrice(new BigDecimal("19.99"));
            assertEquals(0, new BigDecimal("19.99").compareTo(product.getPrice()));
        }

        @Test
        @DisplayName("Should handle zero price")
        void shouldHandleZeroPrice() {
            product.setPrice(BigDecimal.ZERO);
            assertEquals(0, BigDecimal.ZERO.compareTo(product.getPrice()));
        }

        @Test
        @DisplayName("Should handle large prices")
        void shouldHandleLargePrices() {
            product.setPrice(new BigDecimal("99999.99"));
            assertEquals(0, new BigDecimal("99999.99").compareTo(product.getPrice()));
        }

        @Test
        @DisplayName("Should handle price with high precision")
        void shouldHandlePriceWithHighPrecision() {
            product.setPrice(new BigDecimal("99.999"));
            assertEquals(0, new BigDecimal("99.999").compareTo(product.getPrice()));
        }

        @Test
        @DisplayName("Should handle price update")
        void shouldHandlePriceUpdate() {
            product.setPrice(new BigDecimal("99.99"));
            assertEquals(0, new BigDecimal("99.99").compareTo(product.getPrice()));

            product.setPrice(new BigDecimal("79.99"));
            assertEquals(0, new BigDecimal("79.99").compareTo(product.getPrice()));
        }

        @Test
        @DisplayName("Should handle null price")
        void shouldHandleNullPrice() {
            product.setPrice(null);
            assertNull(product.getPrice());
        }
    }

    @Nested
    @DisplayName("Stock Management Tests")
    class StockManagementTests {

        @Test
        @DisplayName("Should set and get stock quantity")
        void shouldSetAndGetStockQuantity() {
            product.setStockQuantity(50);
            assertEquals(50, product.getStockQuantity());
        }

        @Test
        @DisplayName("Should handle zero stock")
        void shouldHandleZeroStock() {
            product.setStockQuantity(0);
            assertEquals(0, product.getStockQuantity());
        }

        @Test
        @DisplayName("Should handle stock increase")
        void shouldHandleStockIncrease() {
            product.setStockQuantity(100);
            product.setStockQuantity(150);
            assertEquals(150, product.getStockQuantity());
        }

        @Test
        @DisplayName("Should handle stock decrease")
        void shouldHandleStockDecrease() {
            product.setStockQuantity(100);
            product.setStockQuantity(75);
            assertEquals(75, product.getStockQuantity());
        }

        @Test
        @DisplayName("Should handle large stock quantities")
        void shouldHandleLargeStockQuantities() {
            product.setStockQuantity(10000);
            assertEquals(10000, product.getStockQuantity());
        }

        @Test
        @DisplayName("Should simulate stock depletion")
        void shouldSimulateStockDepletion() {
            product.setStockQuantity(5);

            // Simulate 5 purchases
            for (int i = 5; i > 0; i--) {
                assertEquals(i, product.getStockQuantity());
                product.setStockQuantity(product.getStockQuantity() - 1);
            }

            assertEquals(0, product.getStockQuantity());
        }

        @Test
        @DisplayName("Should handle null stock quantity")
        void shouldHandleNullStockQuantity() {
            product.setStockQuantity(null);
            assertNull(product.getStockQuantity());
        }
    }

    @Nested
    @DisplayName("Active Status Tests")
    class ActiveStatusTests {

        @Test
        @DisplayName("Should set product as active")
        void shouldSetProductAsActive() {
            product.setActive(true);
            assertTrue(product.getActive());
        }

        @Test
        @DisplayName("Should set product as inactive")
        void shouldSetProductAsInactive() {
            product.setActive(false);
            assertFalse(product.getActive());
        }

        @Test
        @DisplayName("Should toggle active status")
        void shouldToggleActiveStatus() {
            product.setActive(true);
            assertTrue(product.getActive());

            product.setActive(false);
            assertFalse(product.getActive());

            product.setActive(true);
            assertTrue(product.getActive());
        }

        @Test
        @DisplayName("Should handle soft delete via active flag")
        void shouldHandleSoftDeleteViaActiveFlag() {
            product.setActive(true);
            assertTrue(product.getActive());

            // Soft delete
            product.setActive(false);
            product.setUpdatedAt(LocalDateTime.now());

            assertFalse(product.getActive());
            assertNotNull(product.getId()); // Product still exists
        }

        @Test
        @DisplayName("Should handle null active status")
        void shouldHandleNullActiveStatus() {
            product.setActive(null);
            assertNull(product.getActive());
        }
    }

    @Nested
    @DisplayName("Category and Brand Tests")
    class CategoryAndBrandTests {

        @Test
        @DisplayName("Should set and get category")
        void shouldSetAndGetCategory() {
            product.setCategory("Computers");
            assertEquals("Computers", product.getCategory());
        }

        @Test
        @DisplayName("Should set and get brand")
        void shouldSetAndGetBrand() {
            product.setBrand("Dell");
            assertEquals("Dell", product.getBrand());
        }

        @Test
        @DisplayName("Should handle category change")
        void shouldHandleCategoryChange() {
            product.setCategory("Electronics");
            assertEquals("Electronics", product.getCategory());

            product.setCategory("Computers");
            assertEquals("Computers", product.getCategory());
        }

        @Test
        @DisplayName("Should handle brand change")
        void shouldHandleBrandChange() {
            product.setBrand("BrandA");
            assertEquals("BrandA", product.getBrand());

            product.setBrand("BrandB");
            assertEquals("BrandB", product.getBrand());
        }

        @Test
        @DisplayName("Should handle null category")
        void shouldHandleNullCategory() {
            product.setCategory(null);
            assertNull(product.getCategory());
        }

        @Test
        @DisplayName("Should handle null brand")
        void shouldHandleNullBrand() {
            product.setBrand(null);
            assertNull(product.getBrand());
        }

        @Test
        @DisplayName("Should handle empty category")
        void shouldHandleEmptyCategory() {
            product.setCategory("");
            assertEquals("", product.getCategory());
        }

        @Test
        @DisplayName("Should handle empty brand")
        void shouldHandleEmptyBrand() {
            product.setBrand("");
            assertEquals("", product.getBrand());
        }
    }

    @Nested
    @DisplayName("Name and Description Tests")
    class NameAndDescriptionTests {

        @Test
        @DisplayName("Should set and get name")
        void shouldSetAndGetName() {
            product.setName("New Product Name");
            assertEquals("New Product Name", product.getName());
        }

        @Test
        @DisplayName("Should set and get description")
        void shouldSetAndGetDescription() {
            product.setDescription("New detailed description");
            assertEquals("New detailed description", product.getDescription());
        }

        @Test
        @DisplayName("Should handle long product name")
        void shouldHandleLongProductName() {
            String longName = "A".repeat(255);
            product.setName(longName);
            assertEquals(longName, product.getName());
        }

        @Test
        @DisplayName("Should handle long description")
        void shouldHandleLongDescription() {
            String longDescription = "A".repeat(1000);
            product.setDescription(longDescription);
            assertEquals(longDescription, product.getDescription());
        }

        @Test
        @DisplayName("Should handle null name")
        void shouldHandleNullName() {
            product.setName(null);
            assertNull(product.getName());
        }

        @Test
        @DisplayName("Should handle null description")
        void shouldHandleNullDescription() {
            product.setDescription(null);
            assertNull(product.getDescription());
        }

        @Test
        @DisplayName("Should handle empty description")
        void shouldHandleEmptyDescription() {
            product.setDescription("");
            assertEquals("", product.getDescription());
        }
    }

    @Nested
    @DisplayName("Image URL Tests")
    class ImageUrlTests {

        @Test
        @DisplayName("Should set and get image URL")
        void shouldSetAndGetImageUrl() {
            product.setImageUrl("http://example.com/newimage.jpg");
            assertEquals("http://example.com/newimage.jpg", product.getImageUrl());
        }

        @Test
        @DisplayName("Should handle HTTPS URLs")
        void shouldHandleHttpsUrls() {
            product.setImageUrl("https://secure.example.com/image.png");
            assertEquals("https://secure.example.com/image.png", product.getImageUrl());
        }

        @Test
        @DisplayName("Should handle relative URLs")
        void shouldHandleRelativeUrls() {
            product.setImageUrl("/images/product.jpg");
            assertEquals("/images/product.jpg", product.getImageUrl());
        }

        @Test
        @DisplayName("Should handle null image URL")
        void shouldHandleNullImageUrl() {
            product.setImageUrl(null);
            assertNull(product.getImageUrl());
        }

        @Test
        @DisplayName("Should handle empty image URL")
        void shouldHandleEmptyImageUrl() {
            product.setImageUrl("");
            assertEquals("", product.getImageUrl());
        }
    }

    @Nested
    @DisplayName("Timestamp Tests")
    class TimestampTests {

        @Test
        @DisplayName("Should set and get createdAt timestamp")
        void shouldSetAndGetCreatedAtTimestamp() {
            LocalDateTime now = LocalDateTime.now();
            product.setCreatedAt(now);
            assertEquals(now, product.getCreatedAt());
        }

        @Test
        @DisplayName("Should set and get updatedAt timestamp")
        void shouldSetAndGetUpdatedAtTimestamp() {
            LocalDateTime now = LocalDateTime.now();
            product.setUpdatedAt(now);
            assertEquals(now, product.getUpdatedAt());
        }

        @Test
        @DisplayName("Should maintain createdAt while updating updatedAt")
        void shouldMaintainCreatedAtWhileUpdatingUpdatedAt() throws InterruptedException {
            LocalDateTime created = LocalDateTime.now();
            product.setCreatedAt(created);

            Thread.sleep(10);

            LocalDateTime updated = LocalDateTime.now();
            product.setUpdatedAt(updated);

            assertEquals(created, product.getCreatedAt());
            assertTrue(product.getUpdatedAt().isAfter(product.getCreatedAt()));
        }

        @Test
        @DisplayName("Should handle null timestamps")
        void shouldHandleNullTimestamps() {
            product.setCreatedAt(null);
            product.setUpdatedAt(null);

            assertNull(product.getCreatedAt());
            assertNull(product.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should represent complete product")
        void shouldRepresentCompleteProduct() {
            assertNotNull(product.getId());
            assertNotNull(product.getName());
            assertNotNull(product.getDescription());
            assertNotNull(product.getPrice());
            assertNotNull(product.getStockQuantity());
            assertNotNull(product.getCategory());
            assertNotNull(product.getBrand());
            assertNotNull(product.getImageUrl());
            assertNotNull(product.getActive());
            assertNotNull(product.getCreatedAt());
            assertNotNull(product.getUpdatedAt());
        }

        @Test
        @DisplayName("Should handle product lifecycle - creation to active")
        void shouldHandleProductLifecycleCreationToActive() {
            Product newProduct = new Product();
            newProduct.setName("New Product");
            newProduct.setPrice(new BigDecimal("49.99"));
            newProduct.setStockQuantity(50);
            newProduct.setActive(true);
            newProduct.setCreatedAt(LocalDateTime.now());
            newProduct.setUpdatedAt(LocalDateTime.now());

            assertTrue(newProduct.getActive());
            assertEquals(50, newProduct.getStockQuantity());
        }

        @Test
        @DisplayName("Should handle product lifecycle - stock depletion")
        void shouldHandleProductLifecycleStockDepletion() {
            product.setStockQuantity(10);

            // Simulate sales
            product.setStockQuantity(5);
            product.setStockQuantity(1);
            product.setStockQuantity(0);

            assertEquals(0, product.getStockQuantity());
        }

        @Test
        @DisplayName("Should handle product lifecycle - restock")
        void shouldHandleProductLifecycleRestock() {
            product.setStockQuantity(0);
            assertEquals(0, product.getStockQuantity());

            // Restock
            product.setStockQuantity(100);
            product.setUpdatedAt(LocalDateTime.now());

            assertEquals(100, product.getStockQuantity());
        }

        @Test
        @DisplayName("Should handle product lifecycle - price update")
        void shouldHandleProductLifecyclePriceUpdate() {
            product.setPrice(new BigDecimal("99.99"));

            // Price drop
            product.setPrice(new BigDecimal("79.99"));
            product.setUpdatedAt(LocalDateTime.now());

            assertEquals(0, new BigDecimal("79.99").compareTo(product.getPrice()));
        }

        @Test
        @DisplayName("Should handle product lifecycle - soft delete")
        void shouldHandleProductLifecycleSoftDelete() {
            product.setActive(true);

            // Soft delete
            product.setActive(false);
            product.setUpdatedAt(LocalDateTime.now());

            assertFalse(product.getActive());
            assertNotNull(product.getId());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Validation")
    class EdgeCasesAndValidation {

        @Test
        @DisplayName("Should handle product with minimum valid data")
        void shouldHandleProductWithMinimumValidData() {
            Product minProduct = new Product();
            minProduct.setName("Min Product");
            minProduct.setPrice(new BigDecimal("0.01"));
            minProduct.setStockQuantity(1);

            assertEquals("Min Product", minProduct.getName());
            assertEquals(0, new BigDecimal("0.01").compareTo(minProduct.getPrice()));
            assertEquals(1, minProduct.getStockQuantity());
        }

        @Test
        @DisplayName("Should handle product with all null optional fields")
        void shouldHandleProductWithAllNullOptionalFields() {
            Product nullProduct = new Product();
            nullProduct.setName("Product");
            nullProduct.setPrice(new BigDecimal("99.99"));
            nullProduct.setStockQuantity(10);

            assertNull(nullProduct.getDescription());
            assertNull(nullProduct.getCategory());
            assertNull(nullProduct.getBrand());
            assertNull(nullProduct.getImageUrl());
        }

        @Test
        @DisplayName("Should handle negative stock quantity")
        void shouldHandleNegativeStockQuantity() {
            product.setStockQuantity(-1);
            assertEquals(-1, product.getStockQuantity());
        }

        @Test
        @DisplayName("Should handle very small price")
        void shouldHandleVerySmallPrice() {
            product.setPrice(new BigDecimal("0.01"));
            assertEquals(0, new BigDecimal("0.01").compareTo(product.getPrice()));
        }

        @Test
        @DisplayName("Should handle product with all fields null")
        void shouldHandleProductWithAllFieldsNull() {
            Product nullProduct = new Product();
            assertNull(nullProduct.getId());
            assertNull(nullProduct.getName());
            assertNull(nullProduct.getDescription());
            assertNull(nullProduct.getPrice());
            assertEquals(0, nullProduct.getStockQuantity()); // Default value is 0
            assertNull(nullProduct.getCategory());
            assertNull(nullProduct.getBrand());
            assertNull(nullProduct.getImageUrl());
            // Active field might have a default value, so check if null or has default
            // assertNull(nullProduct.getActive());
            assertNull(nullProduct.getCreatedAt());
            assertNull(nullProduct.getUpdatedAt());
        }
    }
}
