package org.example.modules.checkout.payment;

import java.util.List;

public interface CheckoutSessionGateway {
    StripeCheckoutSession createHostedCheckoutSession(Long orderId,
                                                      String currency,
                                                      List<StripeCheckoutLineItem> lineItems,
                                                      String idempotencyKey);
}
