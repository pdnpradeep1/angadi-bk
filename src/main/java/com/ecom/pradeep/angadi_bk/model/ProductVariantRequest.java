package com.ecom.pradeep.angadi_bk.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
public class ProductVariantRequest {
    private Long id;
    private String sku;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private int stockQuantity;
    private String imageUrl;
    private Map<String, String> attributes = new HashMap<>();
}