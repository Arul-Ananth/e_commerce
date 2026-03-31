package com.ecommerce.platform.modules.cart.repository;

import com.ecommerce.platform.modules.cart.model.Cart;
import com.ecommerce.platform.modules.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}

