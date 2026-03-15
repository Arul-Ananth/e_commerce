package org.example.modules.checkout.controller;

import org.example.modules.cart.service.CartService;
import org.example.modules.checkout.dto.CheckoutResponse;
import org.example.modules.users.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {

    private final CartService cartService;

    public CheckoutController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CheckoutResponse> checkout(@AuthenticationPrincipal User user) {
        String orderId = UUID.randomUUID().toString();
        cartService.clear(user);
        return ResponseEntity.ok(new CheckoutResponse(orderId, "SUCCESS", "Order placed successfully!"));
    }
}
