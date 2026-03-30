package org.example.modules.checkout.payment.core;

public record PaymentVerifyRequest(
        String payload,
        String signature
) {
}
