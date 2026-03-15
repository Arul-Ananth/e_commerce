package org.example.modules.reviews.controller;

import jakarta.validation.Valid;
import org.example.modules.reviews.dto.request.AddReviewRequest;
import org.example.modules.reviews.dto.response.ReviewResponse;
import org.example.modules.reviews.service.ReviewService;
import org.example.modules.users.model.User;
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
    public List<ReviewResponse> getReviews(@PathVariable("productId") Long productId) {
        return reviewService.getReviewsByProductId(productId);
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> addReview(@PathVariable("productId") Long productId,
                                                    @Valid @RequestBody AddReviewRequest request,
                                                    @AuthenticationPrincipal User user) {
        return ResponseEntity.status(201)
                .body(reviewService.addReview(productId, request, user.getRealUsername()));
    }
}
