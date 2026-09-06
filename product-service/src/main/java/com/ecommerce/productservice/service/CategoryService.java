package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.request.CategoryRequest;
import com.ecommerce.productservice.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategory(String id);

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(String id, CategoryRequest request);

    void deleteCategory(String id);
}
