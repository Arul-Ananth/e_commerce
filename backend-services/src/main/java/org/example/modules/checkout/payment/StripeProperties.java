package org.example.modules.checkout.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StripeProperties {

    @Value("${app.stripe.secret-key:}")
    private String secretKey;

    @Value("${app.stripe.publishable-key:}")
    private String publishableKey;

    @Value("${app.stripe.webhook-secret:}")
    private String webhookSecret;

    @Value("${app.stripe.api-base-url:https://api.stripe.com/v1}")
    private String apiBaseUrl;

    @Value("${app.stripe.success-url:http://localhost:5173/checkout/success}")
    private String successUrl;

    @Value("${app.stripe.cancel-url:http://localhost:5173/checkout/cancel}")
    private String cancelUrl;

    @Value("${app.stripe.currency:usd}")
    private String currency;

    @Value("${app.stripe.connect-timeout-ms:3000}")
    private int connectTimeoutMs;

    @Value("${app.stripe.read-timeout-ms:10000}")
    private int readTimeoutMs;

    @Value("${app.stripe.max-attempts:2}")
    private int maxAttempts;

    public String getSecretKey() {
        return secretKey;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public String getPublishableKey() {
        return publishableKey;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public String getCurrency() {
        return currency;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }
}
