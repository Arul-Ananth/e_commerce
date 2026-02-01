package org.example.modules.auth.controller;

import org.example.modules.auth.dto.AuthResponse;
import org.example.modules.auth.dto.LoginRequest;
import org.example.modules.auth.dto.SignupRequest;
import org.example.modules.auth.service.AuthService;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")

public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest req) {
        return ResponseEntity.ok(auth.signup(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(auth.login(req.getEmail(), req.getPassword()));
    }



}

