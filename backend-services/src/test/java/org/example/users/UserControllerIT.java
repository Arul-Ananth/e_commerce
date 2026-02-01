package org.example.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.modules.users.model.User;
import org.example.testsupport.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerIT extends IntegrationTestBase {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void list_users_requires_admin_or_manager() throws Exception {
        User user = createUser("basic@example.com", "secret123", "ROLE_USER");
        String token = tokenFor(user);

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_list_users_and_flag() throws Exception {
        User admin = createUser("admin2@example.com", "secret123", "ROLE_ADMIN");
        User target = createUser("target@example.com", "secret123", "ROLE_USER");
        String token = tokenFor(admin);

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));

        User manager = createUser("manager@example.com", "secret123", "ROLE_MANAGER");
        String managerToken = tokenFor(manager);
        mockMvc.perform(patch("/api/v1/users/{id}/flag", target.getId())
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flagged", is(true)));
    }

    @Test
    void admin_can_update_user_discount() throws Exception {
        User admin = createUser("admin3@example.com", "secret123", "ROLE_ADMIN");
        User target = createUser("discount@example.com", "secret123", "ROLE_USER");
        String token = tokenFor(admin);

        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "percentage", 10,
                "startDate", "2025-01-01",
                "endDate", "2025-12-31"
        ));

        mockMvc.perform(patch("/api/v1/users/{id}/discount", target.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userDiscountPercentage", is(10.0)));
    }

    @Test
    void admin_can_create_manager() throws Exception {
        User admin = createUser("admin4@example.com", "secret123", "ROLE_ADMIN");
        String token = tokenFor(admin);

        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "email", "manager2@example.com",
                "password", "secret123",
                "username", "manager2"
        ));

        mockMvc.perform(post("/api/v1/users/managers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email", is("manager2@example.com")));
    }

    @Test
    void admin_can_set_employee_role_and_delete_user() throws Exception {
        User admin = createUser("admin5@example.com", "secret123", "ROLE_ADMIN");
        User target = createUser("employee@example.com", "secret123", "ROLE_USER");
        String token = tokenFor(admin);

        String body = objectMapper.writeValueAsString(java.util.Map.of("enabled", true));

        mockMvc.perform(patch("/api/v1/users/{id}/employee", target.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", hasItem("ROLE_EMPLOYEE")));

        mockMvc.perform(delete("/api/v1/users/{id}", target.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}
