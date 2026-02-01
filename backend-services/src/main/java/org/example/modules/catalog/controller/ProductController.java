package org.example.modules.catalog.controller;

import org.example.modules.catalog.model.Product;
import org.example.modules.catalog.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")

public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    // GET /api/v1/products
    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return service.getAllProducts();
    }

    // GET /api/v1/products/{id}
    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.getProductById(id));
    }

    // GET /api/v1/categories
    @GetMapping("/categories")
    public List<String> getCategories() {
        return service.getAllCategories();
    }

    // GET /api/v1/products?category=Electronics (Optional filter style)
    // or GET /api/v1/products/category/{name}
    @GetMapping("/products/category/{categoryName}")
    public List<Product> getByCategory(@PathVariable("categoryName") String categoryName) {
        return service.getProductsByCategory(categoryName);
    }

    // --- Admin Endpoints ---

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.status(201).body(service.createProduct(product));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable("id") Long id, @RequestBody Product product) {
        return ResponseEntity.ok(service.updateProduct(id, product));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long id) {
        service.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}

