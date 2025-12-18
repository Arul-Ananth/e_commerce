package org.example.dto.cart;

import java.math.BigDecimal;

public class CartItemDto {
    private Long id;           // product id
    private String title;
    private BigDecimal price;  // assuming Product.price is BigDecimal
    private String imageUrl;
    private int quantity;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public BigDecimal getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public int getQuantity() { return quantity; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}