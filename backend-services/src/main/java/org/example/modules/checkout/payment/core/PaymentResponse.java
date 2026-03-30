package org.example.modules.checkout.payment.core;

import org.example.modules.checkout.model.PaymentProvider;
import org.example.modules.checkout.model.PaymentStatus;

import java.time.Instant;

public record PaymentResponse(
        PaymentProvider provider,
        PaymentStatus status,
        String checkoutUrl,
        String providerReferenceId,
        String paymentReferenceId,
        Instant expiresAt,
        String message
) {
}
