package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.request.ProductRequest;
import com.ecommerce.productservice.dto.response.PagedResponse;
import com.ecommerce.productservice.dto.response.ProductResponse;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.exception.ResourceNotFoundException;
import com.ecommerce.productservice.mapper.ProductMapper;
import com.ecommerce.productservice.repository.ProductRepository;
import com.ecommerce.productservice.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void getProduct_existingId_returnsProduct() {
        Product product = Product.builder().id("p1").name("Widget").price(BigDecimal.TEN).build();
        when(productRepository.findById("p1")).thenReturn(Optional.of(product));
        when(productMapper.toProductResponse(product))
                .thenReturn(ProductResponse.builder().id("p1").name("Widget").build());

        ProductResponse response = productService.getProduct("p1");

        assertEquals("Widget", response.getName());
    }

    @Test
    void getProduct_unknownId_throwsResourceNotFound() {
        when(productRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProduct("missing"));
    }

    @Test
    void createProduct_validRequest_savesAndReturnsProduct() {
        ProductRequest request = new ProductRequest(
                "Widget", "A useful widget", BigDecimal.valueOf(19.99), null,
                "Acme", "cat-1", "http://example.com/img.png", true);

        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productMapper.toProductResponse(any(Product.class)))
                .thenReturn(ProductResponse.builder().name("Widget").build());

        ProductResponse response = productService.createProduct(request);

        assertEquals("Widget", response.getName());
    }

    @Test
    void deleteProduct_unknownId_throwsResourceNotFound() {
        when(productRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct("missing"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchProducts_appliesFiltersAndReturnsPagedResponse() {
        Product product = Product.builder().id("p1").name("Widget").price(BigDecimal.TEN).build();
        Page<Product> page = new PageImpl<>(List.of(product));

        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(productMapper.toProductResponse(any(Product.class)))
                .thenReturn(ProductResponse.builder().id("p1").name("Widget").build());

        PagedResponse<ProductResponse> response = productService.searchProducts(
                "Widget", "cat-1", "Acme", BigDecimal.ONE, BigDecimal.valueOf(100),
                0, 20, "name", "asc");

        assertEquals(1, response.getContent().size());
        assertEquals("Widget", response.getContent().get(0).getName());
    }
}
