package org.example.modules.checkout.payment;

import org.example.modules.checkout.model.PaymentProvider;
import org.example.modules.checkout.model.PaymentStatus;

import java.time.Instant;

public class PaymentResponse {

    private PaymentProvider provider;
    private PaymentStatus status;
    private String checkoutUrl;
    private String providerReferenceId;
    private String paymentReferenceId;
    private Instant expiresAt;
    private String message;

    public PaymentResponse() {
    }

    public PaymentResponse(PaymentProvider provider,
                           PaymentStatus status,
                           String checkoutUrl,
                           String providerReferenceId,
                           String paymentReferenceId,
                           Instant expiresAt,
                           String message) {
        this.provider = provider;
        this.status = status;
        this.checkoutUrl = checkoutUrl;
        this.providerReferenceId = providerReferenceId;
        this.paymentReferenceId = paymentReferenceId;
        this.expiresAt = expiresAt;
        this.message = message;
    }

    public PaymentProvider getProvider() {
        return provider;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public String getProviderReferenceId() {
        return providerReferenceId;
    }

    public String getPaymentReferenceId() {
        return paymentReferenceId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getMessage() {
        return message;
    }
}
