package org.example.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.modules.users.model.User;
import org.example.testsupport.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerIT extends IntegrationTestBase {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void signup_success() throws Exception {
        String body = """
                {"email":"newuser@example.com","password":"secret123","username":"newuser"}
                """;

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andExpect(jsonPath("$.user.email", is("newuser@example.com")));
    }

    @Test
    void login_success() throws Exception {
        User user = createUser("login@example.com", "secret123", "ROLE_USER");

        String body = objectMapper.writeValueAsString(
                java.util.Map.of("email", user.getEmail(), "password", "secret123")
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andExpect(jsonPath("$.user.email", is(user.getEmail())));
    }

    @Test
    void signup_validation_error_returns_standard_error() throws Exception {
        String body = """
                {"email":"","password":""}
                """;

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.path", is("/auth/signup")))
                .andExpect(jsonPath("$.details.email", notNullValue()))
                .andExpect(jsonPath("$.details.password", notNullValue()));
    }
}
