package org.example.checkout;

import org.example.modules.cart.model.Cart;
import org.example.modules.cart.model.CartItem;
import org.example.modules.catalog.model.Product;
import org.example.modules.checkout.payment.CheckoutSessionGateway;
import org.example.modules.checkout.payment.StripeCheckoutLineItem;
import org.example.modules.checkout.payment.StripeCheckoutSession;
import org.example.modules.users.model.User;
import org.example.testsupport.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(CheckoutControllerIT.CheckoutTestConfig.class)
public class CheckoutControllerIT extends IntegrationTestBase {

    @TestConfiguration
    static class CheckoutTestConfig {
        @Bean
        @Primary
        CheckoutSessionGateway checkoutSessionGateway() {
            return new CheckoutSessionGateway() {
                @Override
                public StripeCheckoutSession createHostedCheckoutSession(Long orderId,
                                                                         String currency,
                                                                         List<StripeCheckoutLineItem> lineItems,
                                                                         String idempotencyKey) {
                    return new StripeCheckoutSession(
                            "cs_test_" + orderId,
                            "https://checkout.test/session/" + orderId,
                            "pi_test_" + orderId,
                            Instant.parse("2030-01-01T00:00:00Z")
                    );
                }
            };
        }
    }

    @Test
    void checkout_creates_pending_order_and_preserves_cart_until_payment_confirmation() throws Exception {
        User user = createUser("checkout@example.com", "secret123", "ROLE_USER");
        String token = tokenFor(user);
        Product product = createProduct("Camera", 299.99, "Electronics");

        Cart cart = cartRepository.save(new Cart(user));
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(1);
        cartItemRepository.save(item);

        MvcResult result = mockMvc.perform(post("/api/v1/checkout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PENDING_PAYMENT")))
                .andExpect(jsonPath("$.orderId", notNullValue()))
                .andExpect(jsonPath("$.checkoutUrl", startsWith("https://checkout.test/session/")))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Long orderId = new ObjectMapper().readTree(response).path("orderId").asLong();

        mockMvc.perform(get("/api/v1/checkout/{orderId}", orderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PENDING_PAYMENT")))
                .andExpect(jsonPath("$.paymentStatus", is("PENDING")));

        mockMvc.perform(get("/api/v1/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)));
    }
}
