package org.example.dto.auth;

import java.util.List;

public class UserDto {
    private Long id;
    private String email;
    private String username; // Added
    private List<String> roles; // Added

    public UserDto() {}

    public UserDto(Long id, String email, String username, List<String> roles) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.roles = roles;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}