package com.ecom.pradeep.angadi_bk.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonSetter;

@Data
public class ProductVariantRequest {
    private Long variantId;
    private Long productId;  // New field to support composite key
    private String sku;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private int stockQuantity;
    private String imageUrl;
    private Map<String, String> attributes = new HashMap<>();

    // Custom setter to handle various types of variant IDs from frontend
    @JsonSetter("variantId")
    public void setVariantId(Object id) {
        if (id == null) {
            this.variantId = null;
            return;
        }

        if (id instanceof Long) {
            this.variantId = (Long) id;
        } else if (id instanceof Integer) {
            this.variantId = ((Integer) id).longValue();
        } else if (id instanceof String) {
            try {
                this.variantId = Long.parseLong((String) id);
            } catch (NumberFormatException e) {
                // For alphanumeric IDs, generate a new numeric ID
                this.variantId = System.currentTimeMillis();
            }
        } else {
            // Default fallback for any other type
            this.variantId = System.currentTimeMillis();
        }
    }

    // For backward compatibility with the old "id" field
    @JsonSetter("id")
    public void setId(Object id) {
        setVariantId(id);
    }
}