package org.example.controller;

import org.example.dto.auth.AuthResponse;
import org.example.dto.auth.SignupRequest;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = {"http://localhost:5173", "http://192.168.1.4:5173"})
public class UserController {

    private final UserRepository userRepository;
    private final AuthService authService; // Add AuthService

    public UserController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }


    // List all users (For Admin view & Manager to find people to flag)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Flag a user (Manager Only)
    @PatchMapping("/{id}/flag")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<User> flagUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setFlagged(true); // Assuming setFlagged exists in User entity
        return ResponseEntity.ok(userRepository.save(user));
    }

    // Unflag a user (Admin Only - after review)
    @PatchMapping("/{id}/unflag")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> unflagUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setFlagged(false);
        return ResponseEntity.ok(userRepository.save(user));
    }

    // Delete a user (Admin Only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    //  Admin-only creation
    @PostMapping("/managers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> createManager(@RequestBody SignupRequest req) {
        return ResponseEntity.ok(authService.registerManager(req));
    }
}