package org.example.modules.checkout.dto;

public record CheckoutResponse(
        String orderId,
        String status,
        String message
) {
}
