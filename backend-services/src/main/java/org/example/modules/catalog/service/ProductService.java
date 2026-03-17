package org.example.modules.catalog.service;

import org.example.modules.catalog.dto.request.ProductUpsertRequest;
import org.example.modules.catalog.dto.response.DiscountResponse;
import org.example.modules.catalog.dto.response.ProductListItemResponse;
import org.example.modules.catalog.dto.response.ProductResponse;
import org.example.modules.catalog.model.Product;
import org.example.modules.catalog.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductListItemResponse> getProducts(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<Product> products = (category == null || category.isBlank())
                ? productRepository.findPageWithImages(pageable)
                : productRepository.findPageWithImagesByCategory(category, pageable);
        return products.map(this::toListItemResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return toDetailResponse(getProductEntityById(id));
    }

    @Transactional(readOnly = true)
    public Product getProductEntityById(Long id) {
        return productRepository.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return productRepository.findDistinctCategories();
    }

    @Transactional
    public ProductResponse createProduct(ProductUpsertRequest request) {
        Product product = new Product();
        applyUpsertRequest(product, request);
        return toDetailResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpsertRequest request) {
        Product existing = getProductEntityById(id);
        applyUpsertRequest(existing, request);
        return toDetailResponse(productRepository.save(existing));
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        productRepository.deleteById(id);
    }

    private void applyUpsertRequest(Product product, ProductUpsertRequest request) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(request.category());
        product.setPrice(request.price());
        product.setImages(request.images());
    }

    public ProductListItemResponse toListItemResponse(Product product) {
        return new ProductListItemResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice(),
                product.getImages()
        );
    }

    public ProductResponse toDetailResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice(),
                product.getImages(),
                product.getDiscounts().stream()
                        .map(discount -> new DiscountResponse(
                                discount.getId(),
                                discount.getDescription(),
                                discount.getPercentage(),
                                discount.getStartDate(),
                                discount.getEndDate()
                        ))
                        .toList()
        );
    }
}
