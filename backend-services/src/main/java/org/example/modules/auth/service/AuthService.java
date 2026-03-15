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
        if (req.email() == null || req.email().isBlank() ||
                req.password() == null || req.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        User user = new User();
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRealUsername(req.username() != null && !req.username().isBlank()
                ? req.username()
                : req.email().split("@")[0]);

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Role 'ROLE_USER' is not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        user = userRepository.save(user);
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, mapToDto(user));
    }

    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        enforceAccountActive(user);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, mapToDto(user));
    }

    public AuthResponse registerManager(SignupRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        User user = new User();
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRealUsername(req.username() != null ? req.username() : "Manager");

        Role managerRole = roleRepository.findByName("ROLE_MANAGER")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Role 'ROLE_MANAGER' is not found"));

        user.setRoles(Set.of(managerRole));
        user = userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, mapToDto(user));
    }

    private void enforceAccountActive(User user) {
        if (!user.isEnabled() || !user.isAccountNonLocked()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is disabled or locked");
        }
    }

    private UserDto mapToDto(User user) {
        List<String> roleNames = user.getRoles().stream().map(Role::getName).toList();
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
}
