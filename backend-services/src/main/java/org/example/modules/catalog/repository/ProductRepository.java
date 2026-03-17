package org.example.modules.catalog.repository;

import org.example.modules.catalog.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"images"})
    @Query("select p from Product p")
    Page<Product> findPageWithImages(Pageable pageable);

    @EntityGraph(attributePaths = {"images"})
    @Query("select p from Product p where p.category = :category")
    Page<Product> findPageWithImagesByCategory(@Param("category") String category, Pageable pageable);

    @EntityGraph(attributePaths = {"images"})
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findDetailedById(@Param("id") Long id);

    List<Product> findByCategory(String category);

    @Query("select distinct p.category from Product p where p.category is not null order by p.category")
    List<String> findDistinctCategories();
}
