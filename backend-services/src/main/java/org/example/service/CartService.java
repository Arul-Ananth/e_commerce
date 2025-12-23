package org.example.service;

import org.example.dto.cart.CartItemDiscountDto;
import org.example.dto.cart.CartItemDto;
import org.example.dto.cart.CartResponse;
import org.example.model.*;
import org.example.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private static final double EMPLOYEE_DISCOUNT_PERCENTAGE = 15.0;

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
    public CartResponse addOrIncrement(User user, Long productId, int quantity, Long discountId) {
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
            item.setSelectedDiscount(resolveSelectedDiscount(product, discountId, true));
        } else {
            item.setQuantity(item.getQuantity() + quantity); // increment
            if (discountId != null) {
                item.setSelectedDiscount(resolveSelectedDiscount(product, discountId, false));
            }
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

    public CartResponse updateItemDiscount(User user, Long productId, Long discountId) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not in cart"));

        item.setSelectedDiscount(resolveSelectedDiscount(product, discountId, false));
        cartItemRepository.save(item);
        return toResponse(cartRepository.findById(cart.getId()).orElse(cart));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemDto> items = cart.getItems().stream()
                .map(ci -> {
                    var dto = new CartItemDto();
                    Discount activeSelected = getActiveSelectedDiscount(ci.getSelectedDiscount());
                    double productDiscountPercentage = activeSelected != null ? activeSelected.getPercentage() : 0.0;
                    double userDiscountPercentage = getActiveUserDiscountPercentage(ci.getCart().getUser());
                    double employeeDiscountPercentage = isEmployee(ci.getCart().getUser())
                            ? EMPLOYEE_DISCOUNT_PERCENTAGE
                            : 0.0;
                    double totalDiscountPercentage = Math.min(
                            100.0,
                            productDiscountPercentage + userDiscountPercentage + employeeDiscountPercentage
                    );
                    BigDecimal basePrice = BigDecimal.valueOf(ci.getProduct().getPrice());
                    BigDecimal finalPrice = basePrice.multiply(
                            BigDecimal.valueOf(1 - (totalDiscountPercentage / 100.0))
                    );

                    dto.setId(ci.getProduct().getId());
                    // Assuming Product getters: getTitle(), getPrice(), getImageUrl()
                    dto.setTitle(ci.getProduct().getName());
                    dto.setPrice(basePrice);
                    dto.setFinalPrice(finalPrice);
                    dto.setImageUrl(ci.getProduct().getImages().getFirst());
                    dto.setQuantity(ci.getQuantity());
                    dto.setProductDiscount(mapDiscount(activeSelected));
                    dto.setUserDiscountPercentage(userDiscountPercentage);
                    dto.setEmployeeDiscountPercentage(employeeDiscountPercentage);
                    dto.setTotalDiscountPercentage(totalDiscountPercentage);
                    return dto;
                })
                .collect(Collectors.toList());
        return new CartResponse(items);
    }

    private Discount resolveSelectedDiscount(Product product, Long discountId, boolean applyBestIfMissing) {
        if (discountId == null) {
            return applyBestIfMissing ? getBestActiveDiscount(product) : null;
        }
        if (discountId == 0) {
            return null;
        }
        return product.getDiscounts().stream()
                .filter(d -> d.getId().equals(discountId))
                .filter(this::isDiscountActive)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or inactive discount"));
    }

    private Discount getBestActiveDiscount(Product product) {
        return product.getDiscounts().stream()
                .filter(this::isDiscountActive)
                .max(Comparator.comparing(Discount::getPercentage))
                .orElse(null);
    }

    private Discount getActiveSelectedDiscount(Discount selected) {
        if (selected == null) {
            return null;
        }
        return isDiscountActive(selected) ? selected : null;
    }

    private boolean isDiscountActive(Discount discount) {
        LocalDate today = LocalDate.now();
        if (discount.getStartDate() != null && discount.getStartDate().isAfter(today)) {
            return false;
        }
        if (discount.getEndDate() != null && discount.getEndDate().isBefore(today)) {
            return false;
        }
        return true;
    }

    private boolean isEmployee(User user) {
        return user.getRoles().stream().anyMatch(role -> "ROLE_EMPLOYEE".equals(role.getName()));
    }

    private double getActiveUserDiscountPercentage(User user) {
        Double percentage = user.getUserDiscountPercentage();
        if (percentage == null || percentage <= 0) {
            return 0.0;
        }
        LocalDate today = LocalDate.now();
        LocalDate start = user.getUserDiscountStartDate();
        LocalDate end = user.getUserDiscountEndDate();
        if (start == null || start.isAfter(today)) {
            return 0.0;
        }
        if (end != null && end.isBefore(today)) {
            return 0.0;
        }
        return percentage;
    }

    private CartItemDiscountDto mapDiscount(Discount discount) {
        if (discount == null) {
            return null;
        }
        CartItemDiscountDto dto = new CartItemDiscountDto();
        dto.setId(discount.getId());
        dto.setDescription(discount.getDescription());
        dto.setPercentage(discount.getPercentage());
        dto.setStartDate(discount.getStartDate());
        dto.setEndDate(discount.getEndDate());
        return dto;
    }


}
