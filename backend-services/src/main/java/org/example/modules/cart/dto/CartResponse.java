package org.example.modules.cart.dto;

import java.util.List;

public record CartResponse(List<CartItemDto> items) {}
