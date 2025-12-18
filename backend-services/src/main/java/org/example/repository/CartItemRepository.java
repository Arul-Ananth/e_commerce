package org.example.repository;

import org.example.model.CartItem;
import org.example.model.Cart;
import org.example.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    void deleteByCartAndProduct(Cart cart, Product product);
}