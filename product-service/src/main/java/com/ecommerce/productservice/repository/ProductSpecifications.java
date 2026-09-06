package com.ecommerce.productservice.repository;

import com.ecommerce.productservice.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * Composable filter predicates used by ProductServiceImpl to build a
 * single dynamic query for /api/products/search instead of one query
 * method per filter combination.
 */
public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> nameContains(String keyword) {
        return (root, query, cb) -> keyword == null || keyword.isBlank()
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
    }

    public static Specification<Product> hasCategory(String categoryId) {
        return (root, query, cb) -> categoryId == null || categoryId.isBlank()
                ? cb.conjunction()
                : cb.equal(root.get("categoryId"), categoryId);
    }

    public static Specification<Product> hasBrand(String brand) {
        return (root, query, cb) -> brand == null || brand.isBlank()
                ? cb.conjunction()
                : cb.equal(cb.lower(root.get("brand")), brand.toLowerCase());
    }

    public static Specification<Product> priceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, cb) -> minPrice == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> priceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Product> isAvailable(Boolean available) {
        return (root, query, cb) -> available == null
                ? cb.conjunction()
                : cb.equal(root.get("available"), available);
    }
}
