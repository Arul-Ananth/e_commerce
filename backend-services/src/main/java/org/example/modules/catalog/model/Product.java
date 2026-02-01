package org.example.modules.catalog.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Product {
    // ... existing fields (id, name, description, price, category) ...
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String category;
    private double price;

    @ElementCollection
    private List<String> images;

    // --- NEW RELATIONSHIP ---
    // FetchType.EAGER ensures discounts are loaded when Product is loaded
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonManagedReference // Serializes this side of the relationship
    private List<Discount> discounts = new ArrayList<>();

    // --- Helper Method to Add Discount ---
    public void addDiscount(Discount discount) {
        discounts.add(discount);
        discount.setProduct(this);
    }

    // ... Getters and Setters for existing fields ...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    // Getter/Setter for Discounts
    public List<Discount> getDiscounts() { return discounts; }
    public void setDiscounts(List<Discount> discounts) {
        this.discounts = discounts;
        // Maintain the bi-directional link
        for (Discount d : discounts) {
            d.setProduct(this);
        }
    }
}
