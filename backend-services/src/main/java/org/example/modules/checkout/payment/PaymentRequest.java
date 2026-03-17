package org.example.modules.checkout.payment;

import java.util.List;

public class PaymentRequest {

    private Long orderId;
    private String currency;
    private List<PaymentLineItem> lineItems;
    private String idempotencyKey;

    public PaymentRequest() {
    }

    public PaymentRequest(Long orderId, String currency, List<PaymentLineItem> lineItems, String idempotencyKey) {
        this.orderId = orderId;
        this.currency = currency;
        this.lineItems = lineItems;
        this.idempotencyKey = idempotencyKey;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getCurrency() {
        return currency;
    }

    public List<PaymentLineItem> getLineItems() {
        return lineItems;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
