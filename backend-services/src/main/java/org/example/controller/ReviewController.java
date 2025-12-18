package org.example.controller;

import org.example.model.Review;
import org.example.model.User;
import org.example.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/reviews")

public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<Review> getReviews(@PathVariable("productId") Long productId) {
        return reviewService.getReviewsByProductId(productId);
    }

    @PostMapping
    public ResponseEntity<Review> addReview(@PathVariable("productId") Long productId,
                                            @RequestBody Review review,
                                            @AuthenticationPrincipal User user) {
        // Use the real username from the logged-in user
        return ResponseEntity.status(201)
                .body(reviewService.addReview(productId, review, user.getRealUsername()));
    }
}