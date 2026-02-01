package org.example.media;

import org.example.modules.users.model.User;
import org.example.testsupport.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ImageUploadControllerIT extends IntegrationTestBase {

    @Test
    void upload_requires_admin_or_manager() throws Exception {
        User user = createUser("basicupload@example.com", "secret123", "ROLE_USER");
        String token = tokenFor(user);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "data".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/images/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void upload_success_as_admin() throws Exception {
        User admin = createUser("adminupload@example.com", "secret123", "ROLE_ADMIN");
        String token = tokenFor(admin);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "data".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/images/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url", startsWith("http://localhost:8080/images/")));
    }
}
