package org.example.modules.auth.dto;

import java.util.List;

public class UserDto {
    private Long id;
    private String email;
    private String username; // Added
    private List<String> roles; // Added
    private Double userDiscountPercentage;
    private java.time.LocalDate userDiscountStartDate;
    private java.time.LocalDate userDiscountEndDate;

    public UserDto() {}

    public UserDto(Long id, String email, String username, List<String> roles, Double userDiscountPercentage,
                   java.time.LocalDate userDiscountStartDate, java.time.LocalDate userDiscountEndDate) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.roles = roles;
        this.userDiscountPercentage = userDiscountPercentage;
        this.userDiscountStartDate = userDiscountStartDate;
        this.userDiscountEndDate = userDiscountEndDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public Double getUserDiscountPercentage() { return userDiscountPercentage; }
    public void setUserDiscountPercentage(Double userDiscountPercentage) { this.userDiscountPercentage = userDiscountPercentage; }

    public java.time.LocalDate getUserDiscountStartDate() { return userDiscountStartDate; }
    public void setUserDiscountStartDate(java.time.LocalDate userDiscountStartDate) { this.userDiscountStartDate = userDiscountStartDate; }

    public java.time.LocalDate getUserDiscountEndDate() { return userDiscountEndDate; }
    public void setUserDiscountEndDate(java.time.LocalDate userDiscountEndDate) { this.userDiscountEndDate = userDiscountEndDate; }
}
