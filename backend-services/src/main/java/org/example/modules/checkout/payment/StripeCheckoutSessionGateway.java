package org.example.modules.checkout.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Component
public class StripeCheckoutSessionGateway implements CheckoutSessionGateway {

    private static final Logger log = LoggerFactory.getLogger(StripeCheckoutSessionGateway.class);

    private final StripeProperties stripeProperties;
    private final ObjectMapper objectMapper;

    public StripeCheckoutSessionGateway(StripeProperties stripeProperties, ObjectMapper objectMapper) {
        this.stripeProperties = stripeProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public StripeCheckoutSession createHostedCheckoutSession(Long orderId,
                                                             String currency,
                                                             List<StripeCheckoutLineItem> lineItems,
                                                             String idempotencyKey) {
        if (stripeProperties.getSecretKey() == null || stripeProperties.getSecretKey().isBlank()) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Stripe is not configured");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("mode", "payment");
        form.add("success_url", stripeProperties.getSuccessUrl() + "?session_id={CHECKOUT_SESSION_ID}");
        form.add("cancel_url", stripeProperties.getCancelUrl());
        form.add("client_reference_id", String.valueOf(orderId));
        form.add("metadata[orderId]", String.valueOf(orderId));

        for (int i = 0; i < lineItems.size(); i++) {
            StripeCheckoutLineItem item = lineItems.get(i);
            form.add("line_items[" + i + "][price_data][currency]", currency.toLowerCase());
            form.add("line_items[" + i + "][price_data][product_data][name]", item.name());
            form.add("line_items[" + i + "][price_data][unit_amount]", toStripeAmount(item.unitAmount()));
            form.add("line_items[" + i + "][quantity]", String.valueOf(item.quantity()));
        }

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(stripeProperties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(stripeProperties.getReadTimeoutMs());

        RestClient restClient = RestClient.builder()
                .baseUrl(stripeProperties.getApiBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + stripeProperties.getSecretKey())
                .requestFactory(requestFactory)
                .build();

        String responseBody = executeWithRetry(restClient, form, idempotencyKey, orderId);

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String sessionId = root.path("id").asText(null);
            String checkoutUrl = root.path("url").asText(null);
            String paymentIntentId = root.path("payment_intent").asText(null);
            Instant expiresAt = root.hasNonNull("expires_at")
                    ? Instant.ofEpochSecond(root.path("expires_at").asLong())
                    : null;

            if (sessionId == null || checkoutUrl == null) {
                throw new ResponseStatusException(BAD_GATEWAY, "Invalid Stripe checkout response");
            }

            return new StripeCheckoutSession(sessionId, checkoutUrl, paymentIntentId, expiresAt);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unable to parse Stripe checkout response: {}", responseBody, ex);
            throw new ResponseStatusException(BAD_GATEWAY, "Failed to parse Stripe checkout response");
        }
    }

    private String executeWithRetry(RestClient restClient,
                                    MultiValueMap<String, String> form,
                                    String idempotencyKey,
                                    Long orderId) {
        int maxAttempts = Math.max(1, stripeProperties.getMaxAttempts());
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return restClient.post()
                        .uri("/checkout/sessions")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(form)
                        .retrieve()
                        .body(String.class);
            } catch (RestClientResponseException ex) {
                boolean retryable = ex.getStatusCode().is5xxServerError() && attempt < maxAttempts;
                log.error(
                        "Stripe Checkout Session creation failed for orderId={} attempt={}/{} status={} body={}",
                        orderId,
                        attempt,
                        maxAttempts,
                        ex.getStatusCode(),
                        ex.getResponseBodyAsString(),
                        ex
                );
                if (!retryable) {
                    throw new ResponseStatusException(BAD_GATEWAY, "Failed to create Stripe checkout session");
                }
                sleepBeforeRetry();
            } catch (Exception ex) {
                log.error("Stripe Checkout Session creation failed unexpectedly for orderId={} attempt={}/{}",
                        orderId, attempt, maxAttempts, ex);
                if (attempt >= maxAttempts) {
                    throw new ResponseStatusException(BAD_GATEWAY, "Failed to create Stripe checkout session");
                }
                sleepBeforeRetry();
            }
        }
        throw new ResponseStatusException(BAD_GATEWAY, "Failed to create Stripe checkout session");
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(150L);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private String toStripeAmount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
