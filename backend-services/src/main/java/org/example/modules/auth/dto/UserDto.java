package org.example.modules.auth.dto;

import java.time.LocalDate;
import java.util.List;

public record UserDto(
        Long id,
        String email,
        String username,
        List<String> roles,
        Double userDiscountPercentage,
        LocalDate userDiscountStartDate,
        LocalDate userDiscountEndDate
) {}
