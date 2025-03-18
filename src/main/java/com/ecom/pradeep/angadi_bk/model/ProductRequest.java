package com.ecom.pradeep.angadi_bk.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class ProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private int stockQuantity;
    private String sku;
    private int lowStockThreshold = 5;
    private String categoryId;
    private String imageUrl;
    private List<String> additionalImageUrls = new ArrayList<>();
    private boolean featured;
    private String status;
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    private Set<Long> tagIds = new HashSet<>();
    private List<ProductVariantRequest> variants = new ArrayList<>();
}