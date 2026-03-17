package org.example.modules.checkout.payment;

import org.example.modules.checkout.model.PaymentProvider;
import org.example.modules.checkout.model.PaymentStatus;

public class PaymentVerifyResponse {

    private PaymentProvider provider;
    private String eventId;
    private String eventType;
    private boolean supportedEvent;
    private String providerReferenceId;
    private String paymentReferenceId;
    private PaymentStatus paymentStatus;
    private String message;

    public PaymentVerifyResponse() {
    }

    public PaymentVerifyResponse(PaymentProvider provider,
                                 String eventId,
                                 String eventType,
                                 boolean supportedEvent,
                                 String providerReferenceId,
                                 String paymentReferenceId,
                                 PaymentStatus paymentStatus,
                                 String message) {
        this.provider = provider;
        this.eventId = eventId;
        this.eventType = eventType;
        this.supportedEvent = supportedEvent;
        this.providerReferenceId = providerReferenceId;
        this.paymentReferenceId = paymentReferenceId;
        this.paymentStatus = paymentStatus;
        this.message = message;
    }

    public PaymentProvider getProvider() {
        return provider;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public boolean isSupportedEvent() {
        return supportedEvent;
    }

    public String getProviderReferenceId() {
        return providerReferenceId;
    }

    public String getPaymentReferenceId() {
        return paymentReferenceId;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public String getMessage() {
        return message;
    }
}
