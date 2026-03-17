package org.example.modules.checkout.repository;

import org.example.modules.checkout.model.PaymentProvider;
import org.example.modules.checkout.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByProviderSessionId(String providerSessionId);

    Optional<PaymentTransaction> findByPaymentIntentId(String paymentIntentId);

    Optional<PaymentTransaction> findByProviderAndProviderSessionId(PaymentProvider provider, String providerSessionId);

    Optional<PaymentTransaction> findByProviderAndPaymentIntentId(PaymentProvider provider, String paymentIntentId);
}
