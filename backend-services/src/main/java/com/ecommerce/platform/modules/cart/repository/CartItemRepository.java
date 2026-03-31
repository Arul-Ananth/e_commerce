package com.ecommerce.platform.modules.cart.repository;

import com.ecommerce.platform.modules.cart.model.CartItem;
import com.ecommerce.platform.modules.cart.model.Cart;
import com.ecommerce.platform.modules.catalog.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    void deleteByCartAndProduct(Cart cart, Product product);
}

