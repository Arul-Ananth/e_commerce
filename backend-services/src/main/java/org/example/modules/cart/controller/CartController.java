package org.example.modules.cart.controller;

import jakarta.validation.Valid;
import org.example.modules.cart.dto.CartResponse;
import org.example.modules.cart.dto.request.AddCartItemRequest;
import org.example.modules.cart.dto.request.UpdateCartItemDiscountRequest;
import org.example.modules.cart.dto.request.UpdateCartItemQuantityRequest;
import org.example.modules.cart.service.CartService;
import org.example.modules.users.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cartService.getCart(user));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@AuthenticationPrincipal User user,
                                                @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(cartService.addOrIncrement(user, request.productId(), request.quantity(), request.discountId()));
    }

    @PatchMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateQuantity(@AuthenticationPrincipal User user,
                                                       @PathVariable("productId") Long productId,
                                                       @Valid @RequestBody UpdateCartItemQuantityRequest request) {
        return ResponseEntity.ok(cartService.setQuantity(user, productId, request.quantity()));
    }

    @PatchMapping("/items/{productId}/discount")
    public ResponseEntity<CartResponse> updateDiscount(@AuthenticationPrincipal User user,
                                                       @PathVariable("productId") Long productId,
                                                       @Valid @RequestBody UpdateCartItemDiscountRequest request) {
        return ResponseEntity.ok(cartService.updateItemDiscount(user, productId, request.discountId()));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(@AuthenticationPrincipal User user,
                                                   @PathVariable("productId") Long productId) {
        return ResponseEntity.ok(cartService.removeItem(user, productId));
    }

    @DeleteMapping
    public ResponseEntity<CartResponse> clear(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cartService.clear(user));
    }
}
