package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.request.ProductRequest;
import com.ecommerce.productservice.dto.response.PagedResponse;
import com.ecommerce.productservice.dto.response.ProductResponse;

import java.math.BigDecimal;

public interface ProductService {

    ProductResponse getProduct(String id);

    PagedResponse<ProductResponse> getAllProducts(int page, int size, String sortBy, String direction);

    PagedResponse<ProductResponse> searchProducts(
            String keyword, String categoryId, String brand,
            BigDecimal minPrice, BigDecimal maxPrice,
            int page, int size, String sortBy, String direction);

    PagedResponse<ProductResponse> getProductsByCategory(String categoryId, int page, int size);

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(String id, ProductRequest request);

    void deleteProduct(String id);
}
