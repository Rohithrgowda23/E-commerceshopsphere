package com.ecommerce.productservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse implements Serializable {

    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String brand;
    private String categoryId;
    private String imageUrl;
    private BigDecimal rating;
    private boolean available;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
