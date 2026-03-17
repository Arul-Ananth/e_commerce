package org.example.modules.checkout.payment;

import org.example.modules.checkout.model.PaymentProvider;
import org.example.modules.checkout.model.PaymentStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StripePaymentService implements PaymentService {

    private final CheckoutSessionGateway checkoutSessionGateway;
    private final StripeWebhookVerifier stripeWebhookVerifier;

    public StripePaymentService(CheckoutSessionGateway checkoutSessionGateway,
                                StripeWebhookVerifier stripeWebhookVerifier) {
        this.checkoutSessionGateway = checkoutSessionGateway;
        this.stripeWebhookVerifier = stripeWebhookVerifier;
    }

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.STRIPE;
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        List<StripeCheckoutLineItem> lineItems = request.getLineItems().stream()
                .map(item -> new StripeCheckoutLineItem(item.getName(), item.getUnitAmount(), item.getQuantity()))
                .toList();

        StripeCheckoutSession session = checkoutSessionGateway.createHostedCheckoutSession(
                request.getOrderId(),
                request.getCurrency(),
                lineItems,
                request.getIdempotencyKey()
        );

        return new PaymentResponse(
                PaymentProvider.STRIPE,
                PaymentStatus.PENDING,
                session.checkoutUrl(),
                session.sessionId(),
                session.paymentIntentId(),
                session.expiresAt(),
                "Stripe checkout session created"
        );
    }

    @Override
    public PaymentVerifyResponse verifyPayment(PaymentVerifyRequest request) {
        StripeWebhookEvent event = stripeWebhookVerifier.verifyAndParse(request.getPayload(), request.getSignature());
        String type = event.type();

        if ("checkout.session.completed".equals(type)) {
            return new PaymentVerifyResponse(
                    PaymentProvider.STRIPE,
                    event.eventId(),
                    type,
                    true,
                    event.dataObject().path("id").asText(null),
                    blankToNull(event.dataObject().path("payment_intent").asText(null)),
                    PaymentStatus.SUCCEEDED,
                    "Stripe checkout session completed"
            );
        }

        if ("checkout.session.expired".equals(type)) {
            return new PaymentVerifyResponse(
                    PaymentProvider.STRIPE,
                    event.eventId(),
                    type,
                    true,
                    event.dataObject().path("id").asText(null),
                    null,
                    PaymentStatus.EXPIRED,
                    "Stripe checkout session expired"
            );
        }

        if ("payment_intent.payment_failed".equals(type)) {
            return new PaymentVerifyResponse(
                    PaymentProvider.STRIPE,
                    event.eventId(),
                    type,
                    true,
                    null,
                    blankToNull(event.dataObject().path("id").asText(null)),
                    PaymentStatus.FAILED,
                    "Stripe payment failed"
            );
        }

        return new PaymentVerifyResponse(
                PaymentProvider.STRIPE,
                event.eventId(),
                type,
                false,
                null,
                null,
                PaymentStatus.PENDING,
                "Unhandled Stripe event type"
        );
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        return value;
    }
}
