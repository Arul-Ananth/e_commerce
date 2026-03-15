package org.example.modules.cart.dto.request;

import jakarta.validation.constraints.Min;

public record UpdateCartItemQuantityRequest(
        @Min(0) int quantity
) {
}
