package org.example.testsupport;

import org.example.modules.auth.security.JwtService;
import org.example.modules.cart.repository.CartItemRepository;
import org.example.modules.cart.repository.CartRepository;
import org.example.modules.catalog.model.Product;
import org.example.modules.catalog.repository.ProductRepository;
import org.example.modules.users.model.Role;
import org.example.modules.users.model.User;
import org.example.modules.users.repository.RoleRepository;
import org.example.modules.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestBase {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    private static final String UPLOAD_DIR = Path.of(System.getProperty("java.io.tmpdir"), "ecommerce-test-uploads")
            .toAbsolutePath()
            .toString();
    private static final String RESOURCE_LOCATION = Path.of(UPLOAD_DIR).toUri().toString();

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "false");
        registry.add("app.jwt.secret", () -> "test-test-test-test-test-test-test-test-test-test-test-test");
        registry.add("app.cors.allowed-origins", () -> "http://localhost:5173");
        registry.add("app.media.upload-dir", () -> UPLOAD_DIR);
        registry.add("app.media.resource-location", () -> RESOURCE_LOCATION);
        registry.add("app.media.public-base-url", () -> "http://localhost:8080/images/");
    }

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected RoleRepository roleRepository;
    @Autowired
    protected ProductRepository productRepository;
    @Autowired
    protected CartRepository cartRepository;
    @Autowired
    protected CartItemRepository cartItemRepository;
    @Autowired
    protected PasswordEncoder passwordEncoder;
    @Autowired
    protected JwtService jwtService;

    protected Role ensureRole(String name) {
        return roleRepository.findByName(name).orElseGet(() -> roleRepository.save(new Role(name)));
    }

    protected User createUser(String email, String password, String... roles) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRealUsername(email.split("@")[0]);
        Set<Role> roleSet = new java.util.HashSet<>();
        for (String role : roles) {
            roleSet.add(ensureRole(role));
        }
        user.setRoles(roleSet);
        return userRepository.save(user);
    }

    protected String tokenFor(User user) {
        return jwtService.generateToken(user);
    }

    protected Product createProduct(String name, double price, String category) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("Test description");
        product.setCategory(category);
        product.setPrice(price);
        product.setImages(List.of("http://example.com/img.png"));
        return productRepository.save(product);
    }
}
