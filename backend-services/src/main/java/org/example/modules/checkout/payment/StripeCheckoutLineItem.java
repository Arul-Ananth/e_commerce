package org.example.modules.checkout.payment;

import java.math.BigDecimal;

public record StripeCheckoutLineItem(
        String name,
        BigDecimal unitAmount,
        int quantity
) {
}
