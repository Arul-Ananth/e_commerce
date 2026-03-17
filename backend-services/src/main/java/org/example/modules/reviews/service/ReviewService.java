package org.example.modules.reviews.service;

import org.example.modules.catalog.model.Product;
import org.example.modules.catalog.service.ProductService;
import org.example.modules.reviews.dto.request.AddReviewRequest;
import org.example.modules.reviews.dto.response.ReviewResponse;
import org.example.modules.reviews.model.Review;
import org.example.modules.reviews.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductService productService;

    public ReviewService(ReviewRepository reviewRepository, ProductService productService) {
        this.reviewRepository = reviewRepository;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByProductId(Long productId, int page, int size) {
        return reviewRepository.findByProductId(
                        productId,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"))
                )
                .map(this::toResponse);
    }

    @Transactional
    public ReviewResponse addReview(Long productId, AddReviewRequest request, String displayName) {
        var product = productService.getProductEntityById(productId);

        var review = new Review();
        review.setProduct(product);
        review.setUser(displayName);
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
                review.getProduct() != null ? review.getProduct().getId() : null,
                review.getCreatedAt()
        );
    }
}
