package org.example.controller;

import org.example.model.User;
import org.example.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController

@RequestMapping("/api/v1/checkout")

public class CheckoutController {

    private final CartService cartService;

    // Inject CartService so we can empty the cart after purchase
    public CheckoutController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> checkout(@AuthenticationPrincipal User user) {

        // Perform Purchase Logic (Payment Gateway would go here)
        String orderId = UUID.randomUUID().toString();

        //  Clear the User's Cart
        cartService.clear(user);

        // Return Success Response
        return ResponseEntity.ok(Map.of(
                "orderId", orderId,
                "status", "SUCCESS",
                "message", "Order placed successfully!"
        ));
    }
}