package com.ecommerce.productservice.service.impl;

import com.ecommerce.productservice.dto.request.ProductRequest;
import com.ecommerce.productservice.dto.response.PagedResponse;
import com.ecommerce.productservice.dto.response.ProductResponse;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.exception.ResourceNotFoundException;
import com.ecommerce.productservice.mapper.ProductMapper;
import com.ecommerce.productservice.repository.ProductRepository;
import com.ecommerce.productservice.repository.ProductSpecifications;
import com.ecommerce.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
    private static final int MAX_PAGE_SIZE = 100;

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Cacheable(value = "products", key = "#id")
    @Transactional(readOnly = true)
    public ProductResponse getProduct(String id) {
        Product product = findProductOrThrow(id);
        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getAllProducts(int page, int size, String sortBy, String direction) {
        Page<Product> result = productRepository.findAll(buildPageable(page, size, sortBy, direction));
        return toPagedResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> searchProducts(
            String keyword, String categoryId, String brand,
            BigDecimal minPrice, BigDecimal maxPrice,
            int page, int size, String sortBy, String direction) {

        Specification<Product> spec = Specification
                .where(ProductSpecifications.nameContains(keyword))
                .and(ProductSpecifications.hasCategory(categoryId))
                .and(ProductSpecifications.hasBrand(brand))
                .and(ProductSpecifications.priceGreaterThanOrEqual(minPrice))
                .and(ProductSpecifications.priceLessThanOrEqual(maxPrice))
                .and(ProductSpecifications.isAvailable(true));

        Page<Product> result = productRepository.findAll(spec, buildPageable(page, size, sortBy, direction));
        return toPagedResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getProductsByCategory(String categoryId, int page, int size) {
        Specification<Product> spec = Specification
                .where(ProductSpecifications.hasCategory(categoryId))
                .and(ProductSpecifications.isAvailable(true));

        Page<Product> result = productRepository.findAll(spec, buildPageable(page, size, "createdAt", "desc"));
        return toPagedResponse(result);
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .brand(request.getBrand())
                .categoryId(request.getCategoryId())
                .imageUrl(request.getImageUrl())
                .available(request.isAvailable())
                .rating(BigDecimal.ZERO)
                .build();

        Product saved = productRepository.save(product);
        log.info("Created product id={} name={}", saved.getId(), saved.getName());
        return productMapper.toProductResponse(saved);
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public ProductResponse updateProduct(String id, ProductRequest request) {
        Product product = findProductOrThrow(id);

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setBrand(request.getBrand());
        product.setCategoryId(request.getCategoryId());
        product.setImageUrl(request.getImageUrl());
        product.setAvailable(request.isAvailable());

        Product updated = productRepository.save(product);
        log.info("Updated product id={}", id);
        return productMapper.toProductResponse(updated);
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public void deleteProduct(String id) {
        Product product = findProductOrThrow(id);
        productRepository.delete(product);
        log.info("Deleted product id={}", id);
    }

    private Product findProductOrThrow(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No product found with id: " + id));
    }

    private PagedResponse<ProductResponse> toPagedResponse(Page<Product> page) {
        Page<ProductResponse> mapped = page.map(productMapper::toProductResponse);
        return PagedResponse.from(mapped);
    }

    private org.springframework.data.domain.Pageable buildPageable(int page, int size, String sortBy, String direction) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
        Sort.Direction safeDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return PageRequest.of(safePage, safeSize, Sort.by(safeDirection, safeSortBy));
    }
}
