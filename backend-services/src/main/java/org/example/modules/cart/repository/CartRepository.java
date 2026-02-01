package org.example.modules.cart.repository;

import org.example.modules.cart.model.Cart;
import org.example.modules.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}

