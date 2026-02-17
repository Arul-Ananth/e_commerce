package org.example.modules.users.dto;

import java.time.LocalDate;
import java.util.List;

public record UserAdminDto(
        Long id,
        String email,
        String username,
        List<String> roles,
        boolean flagged,
        Double userDiscountPercentage,
        LocalDate userDiscountStartDate,
        LocalDate userDiscountEndDate
) {}
