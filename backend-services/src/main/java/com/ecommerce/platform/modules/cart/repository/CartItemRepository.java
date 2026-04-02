package com.ecommerce.platform.modules.cart.repository;

import com.ecommerce.platform.modules.cart.model.CartItem;
import com.ecommerce.platform.modules.cart.model.Cart;
import com.ecommerce.platform.modules.cart.dto.CartItemView;
import com.ecommerce.platform.modules.catalog.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    void deleteByCartAndProduct(Cart cart, Product product);

    @Query("select ci from CartItem ci where ci.cart.id = :cartId and ci.product.id = :productId")
    Optional<CartItem> findByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);

    @Modifying
    @Query("delete from CartItem ci where ci.cart.id = :cartId and ci.product.id = :productId")
    void deleteByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);

    @Modifying
    @Query("delete from CartItem ci where ci.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") Long cartId);

    @Query("""
            select new com.ecommerce.platform.modules.cart.dto.CartItemView(
                p.id,
                p.name,
                p.price,
                ci.quantity,
                d.id,
                d.description,
                d.percentage,
                d.startDate,
                d.endDate
            )
            from CartItem ci
            join ci.cart c
            join ci.product p
            left join ci.selectedDiscount d
            where c.user.id = :userId
            order by ci.id
            """)
    List<CartItemView> findResponseViewsByUserId(@Param("userId") Long userId);
}

