package org.example.dto.auth;

public class AuthResponse {
    private String token;
    private UserDto user;

    public AuthResponse() {}
    public AuthResponse(String token, UserDto user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public UserDto getUser() { return user; }

    public void setToken(String token) { this.token = token; }
    public void setUser(UserDto user) { this.user = user; }
}