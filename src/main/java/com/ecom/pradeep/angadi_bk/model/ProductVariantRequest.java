package com.ecom.pradeep.angadi_bk.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonSetter;

@Data
public class ProductVariantRequest {
    private Long id;
    private String sku;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private int stockQuantity;
    private String imageUrl;
    private Map<String, String> attributes = new HashMap<>();

    // Custom setter to handle various types of IDs from frontend
    @JsonSetter("id")
    public void setId(Object id) {
        if (id == null) {
            this.id = null;
            return;
        }

        if (id instanceof Long) {
            this.id = (Long) id;
        } else if (id instanceof Integer) {
            this.id = ((Integer) id).longValue();
        } else if (id instanceof String) {
            try {
                this.id = Long.parseLong((String) id);
            } catch (NumberFormatException e) {
                // For alphanumeric IDs, generate a new numeric ID
                this.id = System.currentTimeMillis();
            }
        } else {
            // Default fallback for any other type
            this.id = System.currentTimeMillis();
        }
    }
}