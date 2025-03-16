package com.ecom.pradeep.angadi_bk.model;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class ProductRequest {

//    @NotBlank(message = "Product name is required")
//    @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters")
//    private String name;
//
//    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
//    private String description;
//
//    @NotNull(message = "Price is required")
//    @Min(value = 0, message = "Price cannot be negative")
//    private Double price;
//
//    @NotNull(message = "Category ID is required")
//    private Long categoryId;

    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private int stockQuantity;
    private String categoryId;
    private String imageUrl;
    private String status;

}
