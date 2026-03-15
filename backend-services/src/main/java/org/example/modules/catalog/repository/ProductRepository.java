package org.example.modules.catalog.repository;

import org.example.modules.catalog.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findById(Long id);

    Page<Product> findAll(Pageable pageable);

    Page<Product> findByCategory(String category, Pageable pageable);

    List<Product> findByCategory(String category);

    @Query("select distinct p.category from Product p where p.category is not null order by p.category")
    List<String> findDistinctCategories();
}
