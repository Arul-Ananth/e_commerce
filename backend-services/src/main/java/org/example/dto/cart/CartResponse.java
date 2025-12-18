package org.example.dto.cart;

import java.util.List;

public class CartResponse {
    private List<CartItemDto> items;

    public CartResponse() {}
    public CartResponse(List<CartItemDto> items) { this.items = items; }

    public List<CartItemDto> getItems() { return items; }
    public void setItems(List<CartItemDto> items) { this.items = items; }
}