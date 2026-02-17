package org.example.modules.cart.dto;

import java.time.LocalDate;

public record CartItemDiscountDto(
        Long id,
        String description,
        Double percentage,
        LocalDate startDate,
        LocalDate endDate
) {}
