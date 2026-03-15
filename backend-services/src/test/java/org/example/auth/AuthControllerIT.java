package org.example.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.modules.users.model.User;
import org.example.testsupport.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void login_fails_for_flagged_user() throws Exception {
        User user = createUser("flagged@example.com", "secret123", "ROLE_USER");
        user.setFlagged(true);
        userRepository.save(user);

        String body = objectMapper.writeValueAsString(
                java.util.Map.of("email", user.getEmail(), "password", "secret123")
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("disabled or locked")));
    }

    @Test
    void login_fails_for_disabled_user() throws Exception {
        User user = createUser("disabled@example.com", "secret123", "ROLE_USER");
        user.setEnabled(false);
        userRepository.save(user);

        String body = objectMapper.writeValueAsString(
                java.util.Map.of("email", user.getEmail(), "password", "secret123")
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("disabled or locked")));
    }

    @Test
    void flagged_user_token_is_rejected_on_protected_endpoint() throws Exception {
        User user = createUser("jwt-flagged@example.com", "secret123", "ROLE_USER");
        String token = tokenFor(user);
        user.setFlagged(true);
        userRepository.save(user);

        mockMvc.perform(get("/api/v1/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
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
