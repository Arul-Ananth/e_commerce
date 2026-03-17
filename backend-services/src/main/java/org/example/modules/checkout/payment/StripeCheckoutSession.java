package org.example.modules.checkout.payment;

import java.time.Instant;

public record StripeCheckoutSession(
        String sessionId,
        String checkoutUrl,
        String paymentIntentId,
        Instant expiresAt
) {
}
