package org.example.checkout;

import org.example.modules.cart.model.Cart;
import org.example.modules.cart.model.CartItem;
import org.example.modules.catalog.model.Product;
import org.example.modules.users.model.User;
import org.example.testsupport.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CheckoutControllerIT extends IntegrationTestBase {

    @Test
    void checkout_clears_cart() throws Exception {
        User user = createUser("checkout@example.com", "secret123", "ROLE_USER");
        String token = tokenFor(user);
        Product product = createProduct("Camera", 299.99, "Electronics");

        Cart cart = cartRepository.save(new Cart(user));
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(1);
        cartItemRepository.save(item);

        mockMvc.perform(post("/api/v1/checkout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.orderId", not(emptyString())));
    }
}
