package org.example.service;

import org.example.model.Product;
import org.example.model.Review;
import org.example.repository.ProductRepository;
import org.example.repository.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final ProductRepository productRepo;

    public ReviewService(ReviewRepository reviewRepo, ProductRepository productRepo) {
        this.reviewRepo = reviewRepo;
        this.productRepo = productRepo;
    }

    public List<Review> getReviewsByProductId(Long productId) {
        return reviewRepo.findByProductId(productId);
    }

    public Review addReview(Long productId, Review review, String username) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        review.setProduct(product);
        review.setUser(username); // Set the user from the logged-in token
        return reviewRepo.save(review);
    }
}