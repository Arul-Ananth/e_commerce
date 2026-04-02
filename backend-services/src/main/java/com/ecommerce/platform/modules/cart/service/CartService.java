package com.ecommerce.platform.modules.cart.service;

import com.ecommerce.platform.modules.auth.security.AuthenticatedUser;
import com.ecommerce.platform.modules.cart.dto.CartItemDiscountDto;
import com.ecommerce.platform.modules.cart.dto.CartItemDto;
import com.ecommerce.platform.modules.cart.dto.CartItemView;
import com.ecommerce.platform.modules.cart.dto.CartResponse;
import com.ecommerce.platform.modules.cart.model.Cart;
import com.ecommerce.platform.modules.cart.model.CartItem;
import com.ecommerce.platform.modules.cart.repository.CartItemRepository;
import com.ecommerce.platform.modules.cart.repository.CartRepository;
import com.ecommerce.platform.modules.catalog.model.Discount;
import com.ecommerce.platform.modules.catalog.model.Product;
import com.ecommerce.platform.modules.catalog.service.ProductService;
import com.ecommerce.platform.modules.users.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    private static final BigDecimal EMPLOYEE_DISCOUNT_PERCENTAGE = BigDecimal.valueOf(15);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final int MAX_MUTATION_ATTEMPTS = 2;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductService productService,
                       EntityManager entityManager,
                       PlatformTransactionManager transactionManager) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
        this.entityManager = entityManager;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(AuthenticatedUser user) {
        return buildCartResponse(user);
    }

    public CartResponse addOrIncrement(AuthenticatedUser user, Long productId, int quantity, Long discountId) {
        return withMutationRetry(() -> addOrIncrementInTransaction(user, productId, quantity, discountId));
    }

    public CartResponse setQuantity(AuthenticatedUser user, Long productId, int quantity) {
        return withMutationRetry(() -> setQuantityInTransaction(user, productId, quantity));
    }

    public CartResponse removeItem(AuthenticatedUser user, Long productId) {
        return withMutationRetry(() -> removeItemInTransaction(user, productId));
    }

    @Transactional
    public CartResponse clear(AuthenticatedUser user) {
        clearByUserId(user.getId());
        return new CartResponse(List.of());
    }

    @Transactional
    public void clearByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart != null) {
            cartItemRepository.deleteByCartId(cart.getId());
        }
    }

    public CartResponse updateItemDiscount(AuthenticatedUser user, Long productId, Long discountId) {
        return withMutationRetry(() -> updateItemDiscountInTransaction(user, productId, discountId));
    }

    @Transactional
    protected CartResponse addOrIncrementInTransaction(AuthenticatedUser user, Long productId, int quantity, Long discountId) {
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be > 0");
        }

        Cart cart = getOrCreateCart(user.getId());
        Product product = productService.getProductForCartMutationById(productId);

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId).orElse(null);
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

        cartItemRepository.saveAndFlush(item);
        return buildCartResponse(user);
    }

    @Transactional
    protected CartResponse setQuantityInTransaction(AuthenticatedUser user, Long productId, int quantity) {
        Cart cart = getOrCreateCart(user.getId());
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not in cart"));

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.saveAndFlush(item);
        }

        return buildCartResponse(user);
    }

    @Transactional
    protected CartResponse removeItemInTransaction(AuthenticatedUser user, Long productId) {
        Cart cart = getOrCreateCart(user.getId());
        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
        return buildCartResponse(user);
    }

    @Transactional
    protected CartResponse updateItemDiscountInTransaction(AuthenticatedUser user, Long productId, Long discountId) {
        Cart cart = getOrCreateCart(user.getId());
        Product product = productService.getProductForCartMutationById(productId);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not in cart"));

        item.setSelectedDiscount(resolveSelectedDiscount(product, discountId, false));
        cartItemRepository.saveAndFlush(item);
        return buildCartResponse(user);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> createCart(userId));
    }

    private Cart createCart(Long userId) {
        try {
            Cart cart = new Cart();
            cart.setUser(entityManager.getReference(User.class, userId));
            return cartRepository.saveAndFlush(cart);
        } catch (DataIntegrityViolationException ex) {
            return cartRepository.findByUserId(userId).orElseThrow(() -> ex);
        }
    }

    private CartResponse buildCartResponse(AuthenticatedUser user) {
        List<CartItemView> items = cartItemRepository.findResponseViewsByUserId(user.getId());
        if (items.isEmpty()) {
            return new CartResponse(List.of());
        }

        Map<Long, String> primaryImages = new LinkedHashMap<>(productService.getPrimaryImagesByProductIds(
                items.stream().map(CartItemView::productId).distinct().toList()
        ));

        List<CartItemDto> itemDtos = items.stream()
                .map(item -> toCartItemDto(item, primaryImages.get(item.productId()), user))
                .toList();
        return new CartResponse(itemDtos);
    }

    private CartItemDto toCartItemDto(CartItemView item, String primaryImageUrl, AuthenticatedUser user) {
        Discount activeSelected = getActiveSelectedDiscount(item);
        BigDecimal productDiscountPercentage = activeSelected != null ? activeSelected.getPercentage() : BigDecimal.ZERO;
        BigDecimal userDiscountPercentage = getActiveUserDiscountPercentage(user);
        BigDecimal employeeDiscountPercentage = isEmployee(user)
                ? EMPLOYEE_DISCOUNT_PERCENTAGE
                : BigDecimal.ZERO;
        BigDecimal totalDiscountPercentage = calculateTotalDiscountPercentage(
                productDiscountPercentage,
                userDiscountPercentage,
                employeeDiscountPercentage
        );

        BigDecimal basePrice = item.productPrice();
        BigDecimal finalPrice = calculateFinalPrice(basePrice, totalDiscountPercentage);

        return new CartItemDto(
                item.productId(),
                item.productName(),
                basePrice,
                finalPrice,
                primaryImageUrl,
                item.quantity(),
                mapDiscount(activeSelected),
                userDiscountPercentage,
                employeeDiscountPercentage,
                totalDiscountPercentage
        );
    }

    private BigDecimal calculateTotalDiscountPercentage(BigDecimal productDiscount,
                                                        BigDecimal userDiscount,
                                                        BigDecimal employeeDiscount) {
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

    private Discount getActiveSelectedDiscount(CartItemView selected) {
        if (selected.selectedDiscountId() == null) {
            return null;
        }
        Discount discount = new Discount();
        discount.setId(selected.selectedDiscountId());
        discount.setDescription(selected.selectedDiscountDescription());
        discount.setPercentage(selected.selectedDiscountPercentage());
        discount.setStartDate(selected.selectedDiscountStartDate());
        discount.setEndDate(selected.selectedDiscountEndDate());
        return isDiscountActive(discount) ? discount : null;
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

    private boolean isEmployee(AuthenticatedUser user) {
        return user.hasRole("ROLE_EMPLOYEE");
    }

    private BigDecimal getActiveUserDiscountPercentage(AuthenticatedUser user) {
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

    private CartResponse withMutationRetry(CartMutation mutation) {
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= MAX_MUTATION_ATTEMPTS; attempt++) {
            try {
                return transactionTemplate.execute(status -> mutation.execute());
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException ex) {
                lastFailure = ex;
            }
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Cart was modified concurrently. Please retry.", lastFailure);
    }

    @FunctionalInterface
    private interface CartMutation {
        CartResponse execute();
    }
}
