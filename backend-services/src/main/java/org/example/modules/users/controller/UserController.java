package org.example.modules.users.controller;

import org.example.modules.auth.dto.AuthResponse;
import org.example.modules.auth.dto.SignupRequest;
import org.example.modules.users.dto.UserAdminDto;
import org.example.modules.users.model.Role;
import org.example.modules.users.model.User;
import org.example.modules.users.repository.RoleRepository;
import org.example.modules.users.repository.UserRepository;
import org.example.modules.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")

public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthService authService; // Add AuthService

    public UserController(UserRepository userRepository, RoleRepository roleRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.authService = authService;
    }


    // List all users (For Admin view & Manager to find people to flag)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<UserAdminDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toAdminDto)
                .toList();
    }

    // Flag a user (Manager Only)
    @PatchMapping("/{id}/flag")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<User> flagUser(@PathVariable("id") Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setFlagged(true); // Assuming setFlagged exists in User entity
        return ResponseEntity.ok(userRepository.save(user));
    }

    // Unflag a user (Admin Only - after review)
    @PatchMapping("/{id}/unflag")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> unflagUser(@PathVariable("id") Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setFlagged(false);
        return ResponseEntity.ok(userRepository.save(user));
    }

    // Delete a user (Admin Only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Update per-user discount percentage (Admin or Manager)
    @PatchMapping("/{id}/discount")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<User> updateUserDiscount(@PathVariable("id") Long id,
                                                   @RequestBody java.util.Map<String, Object> body) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Double percentage = null;
        if (body.containsKey("percentage") && body.get("percentage") != null) {
            percentage = ((Number) body.get("percentage")).doubleValue();
        }

        if (percentage == null || percentage < 0 || percentage > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "percentage must be between 0 and 100");
        }

        java.time.LocalDate startDate = parseDate(body.get("startDate"));
        java.time.LocalDate endDate = parseDate(body.get("endDate"));
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate cannot be before startDate");
        }

        user.setUserDiscountPercentage(percentage);
        if (percentage == 0) {
            user.setUserDiscountStartDate(null);
            user.setUserDiscountEndDate(null);
        } else {
            user.setUserDiscountStartDate(startDate);
            user.setUserDiscountEndDate(endDate);
        }
        return ResponseEntity.ok(userRepository.save(user));
    }

    // Toggle employee role (Admin only)
    @PatchMapping("/{id}/employee")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> setEmployeeRole(@PathVariable("id") Long id,
                                                @RequestBody java.util.Map<String, Object> body) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean enabled = Boolean.parseBoolean(String.valueOf(body.get("enabled")));
        Role employeeRole = roleRepository.findByName("ROLE_EMPLOYEE")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Role not found"));

        if (enabled) {
            user.getRoles().add(employeeRole);
        } else {
            user.getRoles().removeIf(role -> "ROLE_EMPLOYEE".equals(role.getName()));
        }

        return ResponseEntity.ok(userRepository.save(user));
    }

    //  Admin-only creation
    @PostMapping("/managers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> createManager(@RequestBody SignupRequest req) {
        return ResponseEntity.ok(authService.registerManager(req));
    }

    private UserAdminDto toAdminDto(User user) {
        UserAdminDto dto = new UserAdminDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getRealUsername());
        dto.setRoles(user.getRoles().stream().map(Role::getName).toList());
        dto.setFlagged(user.isFlagged());
        dto.setUserDiscountPercentage(user.getUserDiscountPercentage());
        dto.setUserDiscountStartDate(user.getUserDiscountStartDate());
        dto.setUserDiscountEndDate(user.getUserDiscountEndDate());
        return dto;
    }

    private java.time.LocalDate parseDate(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty() || "null".equalsIgnoreCase(text)) {
            return null;
        }
        return java.time.LocalDate.parse(text);
    }
}

