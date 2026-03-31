package com.ecommerce.platform.modules.checkout.payment.stripe;

import com.fasterxml.jackson.databind.JsonNode;

public record StripeWebhookEvent(
        String eventId,
        String type,
        JsonNode dataObject
) {
}
