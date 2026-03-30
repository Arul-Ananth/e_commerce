package org.example.modules.checkout.payment.core;

import org.example.modules.checkout.model.PaymentProvider;
import org.example.modules.checkout.model.PaymentStatus;

public record PaymentVerifyResponse(
        PaymentProvider provider,
        String eventId,
        String eventType,
        boolean supportedEvent,
        String providerReferenceId,
        String paymentReferenceId,
        PaymentStatus paymentStatus,
        String message
) {
}
