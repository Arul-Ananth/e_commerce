package org.example.modules.checkout.payment;

public class PaymentVerifyRequest {

    private String payload;
    private String signature;

    public PaymentVerifyRequest() {
    }

    public PaymentVerifyRequest(String payload, String signature) {
        this.payload = payload;
        this.signature = signature;
    }

    public String getPayload() {
        return payload;
    }

    public String getSignature() {
        return signature;
    }
}
