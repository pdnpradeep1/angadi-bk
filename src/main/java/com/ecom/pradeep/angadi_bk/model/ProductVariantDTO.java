package com.ecom.pradeep.angadi_bk.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
public class ProductVariantDTO {
    private Long variantId;
    private Long productId;
    private String sku;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private int stockQuantity;
    private String imageUrl;
    private Map<String, String> attributes = new HashMap<>();

    // Helper method to check if the variant is in stock
    public boolean isInStock() {
        return stockQuantity > 0 || stockQuantity == -1;
    }

    // Helper method to convert from ProductVariant entity
    public static ProductVariantDTO fromProductVariant(ProductVariant variant) {
        ProductVariantDTO dto = new ProductVariantDTO();

        // Set IDs from the composite key
        if (variant.getId() != null) {
            dto.setVariantId(variant.getId().getVariantId());
            dto.setProductId(variant.getId().getProductId());
        }

        dto.setSku(variant.getSku());
        dto.setPrice(variant.getPrice());
        dto.setOriginalPrice(variant.getOriginalPrice());
        dto.setStockQuantity(variant.getStockQuantity());
        dto.setImageUrl(variant.getImageUrl());
        dto.setAttributes(variant.getAttributes());
        return dto;
    }
}