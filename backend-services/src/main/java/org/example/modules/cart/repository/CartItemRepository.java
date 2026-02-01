package org.example.modules.cart.repository;

import org.example.modules.cart.model.CartItem;
import org.example.modules.cart.model.Cart;
import org.example.modules.catalog.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    void deleteByCartAndProduct(Cart cart, Product product);
}

