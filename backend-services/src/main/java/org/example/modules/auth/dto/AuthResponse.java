package org.example.modules.auth.dto;

public record AuthResponse(
        String token,
        UserDto user
) {}
