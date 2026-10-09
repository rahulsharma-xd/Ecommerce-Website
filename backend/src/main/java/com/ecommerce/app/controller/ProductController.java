package com.ecommerce.app.controller;

import com.ecommerce.app.dto.ProductResponse;
import com.ecommerce.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * Get all active products (PUBLIC endpoint)
     * No authentication required
     * Returns only products that are marked as active
     *
     * @return List of active products
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllActiveProducts() {
        List<ProductResponse> products = productService.getAllActiveProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Get product by ID (PUBLIC endpoint)
     * No authentication required
     * Returns product details for the specified ID
     *
     * @param id Product ID
     * @return Product details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }
}
