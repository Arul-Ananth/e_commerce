package org.example.modules.users.dto;

import java.util.List;

public class UserAdminDto {
    private Long id;
    private String email;
    private String username;
    private List<String> roles;
    private boolean flagged;
    private Double userDiscountPercentage;
    private java.time.LocalDate userDiscountStartDate;
    private java.time.LocalDate userDiscountEndDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public boolean isFlagged() { return flagged; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }

    public Double getUserDiscountPercentage() { return userDiscountPercentage; }
    public void setUserDiscountPercentage(Double userDiscountPercentage) { this.userDiscountPercentage = userDiscountPercentage; }

    public java.time.LocalDate getUserDiscountStartDate() { return userDiscountStartDate; }
    public void setUserDiscountStartDate(java.time.LocalDate userDiscountStartDate) { this.userDiscountStartDate = userDiscountStartDate; }

    public java.time.LocalDate getUserDiscountEndDate() { return userDiscountEndDate; }
    public void setUserDiscountEndDate(java.time.LocalDate userDiscountEndDate) { this.userDiscountEndDate = userDiscountEndDate; }
}
