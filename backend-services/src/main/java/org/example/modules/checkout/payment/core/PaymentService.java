package org.example.modules.checkout.payment.core;

import org.example.modules.checkout.model.PaymentProvider;

public interface PaymentService {

    PaymentProvider getProvider();

    PaymentResponse createPayment(PaymentRequest request);

    PaymentVerifyResponse verifyPayment(PaymentVerifyRequest request);
}
