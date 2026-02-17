package org.example.modules.cart.dto;

import java.math.BigDecimal;

public record CartItemDto(
        Long id,
        String title,
        BigDecimal price,
        BigDecimal finalPrice,
        String imageUrl,
        int quantity,
        CartItemDiscountDto productDiscount,
        double userDiscountPercentage,
        double employeeDiscountPercentage,
        double totalDiscountPercentage
) {}
