package org.example.service;

import org.example.dto.cart.CartItemDto;
import org.example.dto.cart.CartResponse;
import org.example.model.*;
import org.example.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user).orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(User user) {
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> new Cart(user));
        return toResponse(cart);
    }

    @Transactional
    public CartResponse addOrIncrement(User user, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be > 0");
        }
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product).orElse(null);
        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
        } else {
            item.setQuantity(item.getQuantity() + quantity); // increment
        }
        cartItemRepository.save(item);
        return toResponse(cartRepository.findById(cart.getId()).orElse(cart));
    }

    @Transactional
    public CartResponse setQuantity(User user, Long productId, int quantity) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not in cart"));

        if (quantity <= 0) {
            cartItemRepository.delete(item); // remove if 0
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
        return toResponse(cartRepository.findById(cart.getId()).orElse(cart));
    }

    @Transactional
    public CartResponse removeItem(User user, Long productId) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        cartItemRepository.deleteByCartAndProduct(cart, product);
        return toResponse(cartRepository.findById(cart.getId()).orElse(cart));
    }

    @Transactional
    public CartResponse clear(User user) {
        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart != null) {
            cart.getItems().clear();
            cartRepository.save(cart);
        }
        return new CartResponse(List.of());
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemDto> items = cart.getItems().stream()
                .map(ci -> {
                    var dto = new CartItemDto();
                    dto.setId(ci.getProduct().getId());
                    // Assuming Product getters: getTitle(), getPrice(), getImageUrl()
                    dto.setTitle(ci.getProduct().getName());
                    dto.setPrice(BigDecimal.valueOf(ci.getProduct().getPrice()));
                    dto.setImageUrl(ci.getProduct().getImages().getFirst());
                    dto.setQuantity(ci.getQuantity());
                    return dto;
                })
                .collect(Collectors.toList());
        return new CartResponse(items);
    }


}