package com.ecom.pradeep.angadi_bk.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@Data
public class ProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private int stockQuantity;
    private String categoryId;
    private String imageUrl;
    private List<String> additionalImageUrls = new ArrayList<>();
    private boolean featured;
    private String status;
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    private Set<Long> tagIds = new HashSet<>();

    // New fields to support the provided structure
    private List<ProductVariantRequest> variants = new ArrayList<>();
    private Map<String, List<String>> optionsMap = new HashMap<>();

    // Getter and setter for variants (to handle the JSON structure)
    public List<ProductVariantRequest> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariantRequest> variants) {
        this.variants = variants;
    }

    // Getter and setter for optionsMap
    public Map<String, List<String>> getOptionsMap() {
        return optionsMap;
    }

    public void setOptionsMap(Map<String, List<String>> optionsMap) {
        this.optionsMap = optionsMap;
    }
}