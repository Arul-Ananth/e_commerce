package org.example.modules.checkout.payment.stripe;

import java.math.BigDecimal;

public record StripeCheckoutLineItem(
        String name,
        BigDecimal unitAmount,
        int quantity
) {
}
