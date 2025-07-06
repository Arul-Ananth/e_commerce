package org.example.service;

import org.example.model.Product;
import org.example.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public List<Product> getAllProducts() {
        return repo.findAll();
    }

    public List<Product> getProductsByCategory(String category) {
        return repo.findByCategory(category);
    }

    public List<String> getAllCategories() {
        return repo.findAll().stream()
                .map(Product::getCategory)
                .distinct()
                .toList();
    }
}
