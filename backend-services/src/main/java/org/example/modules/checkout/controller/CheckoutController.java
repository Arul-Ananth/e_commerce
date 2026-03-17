package org.example.modules.checkout.controller;

import org.example.modules.checkout.dto.CheckoutResponse;
import org.example.modules.checkout.dto.CheckoutStatusResponse;
import org.example.modules.checkout.service.CheckoutService;
import org.example.modules.users.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CheckoutResponse> checkout(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(checkoutService.createCheckoutSession(user));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CheckoutStatusResponse> getOrderStatus(@PathVariable("orderId") Long orderId,
                                                                 @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(checkoutService.getOrderStatus(orderId, user));
    }
}
