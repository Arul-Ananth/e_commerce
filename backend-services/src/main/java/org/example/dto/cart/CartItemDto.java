package org.example.dto.cart;

import java.math.BigDecimal;

public class CartItemDto {
    private Long id;           // product id
    private String title;
    private BigDecimal price;  // assuming Product.price is BigDecimal
    private BigDecimal finalPrice;
    private String imageUrl;
    private int quantity;
    private CartItemDiscountDto productDiscount;
    private double userDiscountPercentage;
    private double employeeDiscountPercentage;
    private double totalDiscountPercentage;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getFinalPrice() { return finalPrice; }
    public String getImageUrl() { return imageUrl; }
    public int getQuantity() { return quantity; }
    public CartItemDiscountDto getProductDiscount() { return productDiscount; }
    public double getUserDiscountPercentage() { return userDiscountPercentage; }
    public double getEmployeeDiscountPercentage() { return employeeDiscountPercentage; }
    public double getTotalDiscountPercentage() { return totalDiscountPercentage; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setProductDiscount(CartItemDiscountDto productDiscount) { this.productDiscount = productDiscount; }
    public void setUserDiscountPercentage(double userDiscountPercentage) { this.userDiscountPercentage = userDiscountPercentage; }
    public void setEmployeeDiscountPercentage(double employeeDiscountPercentage) { this.employeeDiscountPercentage = employeeDiscountPercentage; }
    public void setTotalDiscountPercentage(double totalDiscountPercentage) { this.totalDiscountPercentage = totalDiscountPercentage; }
}
