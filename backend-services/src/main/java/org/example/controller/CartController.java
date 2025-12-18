package org.example.controller;

import org.example.dto.cart.CartResponse;
import org.example.model.User;
import org.example.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
@CrossOrigin(origins = {"http://localhost:5173", "http://192.168.1.4:5173"})
public class CartController {

    private final CartService cart;

    public CartController(CartService cart) {
        this.cart = cart;
    }

    // GET /api/v1/cart
    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cart.getCart(user));
    }

    // POST /api/v1/cart/items
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addOrUpdate(@AuthenticationPrincipal User user,
                                                    @RequestBody Map<String, Object> body) {
        Long productId = ((Number) body.get("productId")).longValue();
        int quantity = ((Number) body.get("quantity")).intValue();
        return ResponseEntity.ok(cart.addOrIncrement(user, productId, quantity));
    }

    // PATCH /api/v1/cart/items/{productId}
    @PatchMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateQuantity(@AuthenticationPrincipal User user,
                                                       @PathVariable Long productId,
                                                       @RequestBody Map<String, Object> body) {
        int quantity = ((Number) body.get("quantity")).intValue();
        return ResponseEntity.ok(cart.setQuantity(user, productId, quantity));
    }

    // DELETE /api/v1/cart/items/{productId}
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(@AuthenticationPrincipal User user,
                                                   @PathVariable Long productId) {
        return ResponseEntity.ok(cart.removeItem(user, productId));
    }

    // DELETE /api/v1/cart (Clear cart)
    @DeleteMapping
    public ResponseEntity<CartResponse> clear(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cart.clear(user));
    }
}