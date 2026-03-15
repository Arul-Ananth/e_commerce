package org.example.modules.reviews.service;

import org.example.modules.catalog.model.Product;
import org.example.modules.catalog.service.ProductService;
import org.example.modules.reviews.dto.request.AddReviewRequest;
import org.example.modules.reviews.dto.response.ReviewResponse;
import org.example.modules.reviews.model.Review;
import org.example.modules.reviews.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductService productService;

    public ReviewService(ReviewRepository reviewRepository, ProductService productService) {
        this.reviewRepository = reviewRepository;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProductId(Long productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ReviewResponse addReview(Long productId, AddReviewRequest request, String username) {
        Product product = productService.getProductEntityById(productId);

        Review review = new Review();
        review.setProduct(product);
        review.setUser(username);
        review.setRating(request.rating());
        review.setComment(request.comment());

        return toResponse(reviewRepository.save(review));
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getUser(),
                review.getComment(),
                review.getRating(),
                review.getProduct() != null ? review.getProduct().getId() : null
        );
    }
}
