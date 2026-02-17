package org.example.modules.auth.service;

import org.example.modules.auth.dto.AuthResponse;
import org.example.modules.auth.dto.SignupRequest;
import org.example.modules.auth.dto.UserDto;
import org.example.modules.auth.security.JwtService;
import org.example.modules.users.model.Role;
import org.example.modules.users.model.User;
import org.example.modules.users.repository.RoleRepository;
import org.example.modules.users.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse signup(SignupRequest req) {
        // Validate Inputs
        if (req.email() == null || req.email().isBlank() ||
                req.password() == null || req.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        // Create User
        User user = new User();
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));

        // Save Username (if provided, otherwise fallback to email prefix or null)
        if (req.username() != null && !req.username().isBlank()) {
            user.setRealUsername(req.username());
        } else {
            user.setRealUsername(req.email().split("@")[0]);
        }

        // Assign Default Role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error: Role 'ROLE_USER' is not found."));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        // Save to DB
        user = userRepository.save(user);

        // Generate Token & Response
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, mapToDto(user));
    }

    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, mapToDto(user));
    }

    // Helper to convert Entity to DTO
    private UserDto mapToDto(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        // Assuming your User entity uses getRealUsername() for the display name
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getRealUsername(),
                roleNames,
                user.getUserDiscountPercentage(),
                user.getUserDiscountStartDate(),
                user.getUserDiscountEndDate()
        );
    }



    public AuthResponse registerManager(SignupRequest req) {
        // Reuse your signup logic but force the role
        if (userRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        User user = new User();
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRealUsername(req.username() != null ? req.username() : "Manager");

        // Assign ROLE_MANAGER
        Role managerRole = roleRepository.findByName("ROLE_MANAGER")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error: Role 'ROLE_MANAGER' is not found."));

        user.setRoles(Set.of(managerRole));
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        // Use the same helper method 'mapToDto' you already have
        return new AuthResponse(token, mapToDto(user));
    }
}

