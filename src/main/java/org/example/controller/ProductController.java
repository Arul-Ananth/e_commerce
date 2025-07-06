package org.example.controller;

import org.example.model.Product;
import org.example.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:5173") // Allow frontend access
public class ProductController {

    private final ProductService service;

    // Constructor-based dependency injection
    public ProductController(ProductService service) {
        this.service = service;
    }

    // Get all products
    @GetMapping
    public List<Product> getAllProducts() {
        return service.getAllProducts();
    }

    // Get products by category
    @GetMapping("/category/{category}")
    public List<Product> getProductsByCategory(@PathVariable String category) {
        return service.getProductsByCategory(category);
    }
    @GetMapping("/categories")
    public List<String> getCategories() {
        return service.getAllCategories();
    }


}
