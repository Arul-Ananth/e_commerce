package org.example.modules.checkout.payment;

import org.example.modules.checkout.model.PaymentProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentServiceResolver {

    private final Map<PaymentProvider, PaymentService> services = new EnumMap<>(PaymentProvider.class);

    @Value("${app.payment.gateway:stripe}")
    private String configuredGateway;

    public PaymentServiceResolver(List<PaymentService> serviceList) {
        for (PaymentService paymentService : serviceList) {
            services.put(paymentService.getProvider(), paymentService);
        }
    }

    public PaymentService resolveConfigured() {
        PaymentProvider provider;
        try {
            provider = PaymentProvider.fromConfigValue(configuredGateway);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
        return resolve(provider);
    }

    public PaymentService resolveByGateway(String gateway) {
        PaymentProvider provider;
        try {
            provider = PaymentProvider.fromConfigValue(gateway);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
        return resolve(provider);
    }

    private PaymentService resolve(PaymentProvider provider) {
        PaymentService paymentService = services.get(provider);
        if (paymentService == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Payment gateway is not available: " + provider.getConfigKey());
        }
        return paymentService;
    }
}
