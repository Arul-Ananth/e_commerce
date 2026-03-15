package org.example.modules.cart.service;

import org.example.modules.cart.dto.CartItemDiscountDto;
import org.example.modules.cart.dto.CartItemDto;
import org.example.modules.cart.dto.CartResponse;
import org.example.modules.cart.model.Cart;
import org.example.modules.cart.model.CartItem;
import org.example.modules.cart.repository.CartItemRepository;
import org.example.modules.cart.repository.CartRepository;
import org.example.modules.catalog.model.Discount;
import org.example.modules.catalog.model.Product;
import org.example.modules.catalog.service.ProductService;
import org.example.modules.users.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class CartService {

    private static final BigDecimal EMPLOYEE_DISCOUNT_PERCENTAGE = BigDecimal.valueOf(15);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductService productService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
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
        Product product = productService.getProductEntityById(productId);

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product).orElse(null);
        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setSelectedDiscount(resolveSelectedDiscount(product, discountId, true));
        } else {
            item.setQuantity(item.getQuantity() + quantity);
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
        Product product = productService.getProductEntityById(productId);

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not in cart"));

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return toResponse(cartRepository.findById(cart.getId()).orElse(cart));
    }

    @Transactional
    public CartResponse removeItem(User user, Long productId) {
        Cart cart = getOrCreateCart(user);
        Product product = productService.getProductEntityById(productId);
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

    @Transactional
    public CartResponse updateItemDiscount(User user, Long productId, Long discountId) {
        Cart cart = getOrCreateCart(user);
        Product product = productService.getProductEntityById(productId);

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not in cart"));

        item.setSelectedDiscount(resolveSelectedDiscount(product, discountId, false));
        cartItemRepository.save(item);
        return toResponse(cartRepository.findById(cart.getId()).orElse(cart));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemDto> items = cart.getItems().stream()
                .map(this::toCartItemDto)
                .toList();
        return new CartResponse(items);
    }

    private CartItemDto toCartItemDto(CartItem item) {
        Discount activeSelected = getActiveSelectedDiscount(item.getSelectedDiscount());
        BigDecimal productDiscountPercentage = activeSelected != null ? activeSelected.getPercentage() : BigDecimal.ZERO;
        BigDecimal userDiscountPercentage = getActiveUserDiscountPercentage(item.getCart().getUser());
        BigDecimal employeeDiscountPercentage = isEmployee(item.getCart().getUser())
                ? EMPLOYEE_DISCOUNT_PERCENTAGE
                : BigDecimal.ZERO;
        BigDecimal totalDiscountPercentage = calculateTotalDiscountPercentage(
                productDiscountPercentage,
                userDiscountPercentage,
                employeeDiscountPercentage
        );

        BigDecimal basePrice = item.getProduct().getPrice();
        BigDecimal finalPrice = calculateFinalPrice(basePrice, totalDiscountPercentage);

        return new CartItemDto(
                item.getProduct().getId(),
                item.getProduct().getName(),
                basePrice,
                finalPrice,
                primaryImage(item.getProduct()),
                item.getQuantity(),
                mapDiscount(activeSelected),
                userDiscountPercentage,
                employeeDiscountPercentage,
                totalDiscountPercentage
        );
    }

    private String primaryImage(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }
        return product.getImages().getFirst();
    }

    private BigDecimal calculateTotalDiscountPercentage(BigDecimal productDiscount,
                                                        BigDecimal userDiscount,
                                                        BigDecimal employeeDiscount) {
        // Discounts are additive by policy but can never exceed 100%.
        BigDecimal combined = productDiscount.add(userDiscount).add(employeeDiscount);
        return combined.min(ONE_HUNDRED).max(BigDecimal.ZERO);
    }

    private BigDecimal calculateFinalPrice(BigDecimal basePrice, BigDecimal totalDiscountPercentage) {
        BigDecimal multiplier = BigDecimal.ONE.subtract(totalDiscountPercentage.divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP));
        return basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
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

    private BigDecimal getActiveUserDiscountPercentage(User user) {
        BigDecimal percentage = user.getUserDiscountPercentage();
        if (percentage == null || percentage.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        LocalDate today = LocalDate.now();
        LocalDate start = user.getUserDiscountStartDate();
        LocalDate end = user.getUserDiscountEndDate();

        if (start == null || start.isAfter(today)) {
            return BigDecimal.ZERO;
        }
        if (end != null && end.isBefore(today)) {
            return BigDecimal.ZERO;
        }
        return percentage;
    }

    private CartItemDiscountDto mapDiscount(Discount discount) {
        if (discount == null) {
            return null;
        }
        return new CartItemDiscountDto(
                discount.getId(),
                discount.getDescription(),
                discount.getPercentage(),
                discount.getStartDate(),
                discount.getEndDate()
        );
    }
}
