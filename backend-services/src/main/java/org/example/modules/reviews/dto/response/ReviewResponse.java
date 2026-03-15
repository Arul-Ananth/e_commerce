package org.example.modules.reviews.dto.response;

public record ReviewResponse(
        Long id,
        String user,
        String comment,
        int rating,
        Long productId
) {
}
