package org.example.reviews;

import org.example.modules.catalog.model.Product;
import org.example.modules.users.model.User;
import org.example.testsupport.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ReviewControllerIT extends IntegrationTestBase {

    @Test
    void get_reviews_empty() throws Exception {
        Product product = createProduct("Book", 15.99, "Books");

        mockMvc.perform(get("/api/v1/products/{id}/reviews", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void add_review_requires_authentication() throws Exception {
        Product product = createProduct("Notebook", 7.99, "Books");
        String body = """
                {"rating": 5, "comment": "Great!"}
                """;

        mockMvc.perform(post("/api/v1/products/{id}/reviews", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void add_review_success() throws Exception {
        User user = createUser("reviewer@example.com", "secret123", "ROLE_USER");
        String token = tokenFor(user);
        Product product = createProduct("Pen", 1.99, "Stationery");

        String body = """
                {"rating": 4, "comment": "Nice"}
                """;

        mockMvc.perform(post("/api/v1/products/{id}/reviews", product.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user", is(user.getRealUsername())))
                .andExpect(jsonPath("$.comment", is("Nice")));
    }
}
