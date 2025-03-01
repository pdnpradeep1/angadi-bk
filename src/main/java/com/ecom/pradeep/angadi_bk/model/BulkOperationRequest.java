package com.ecom.pradeep.angadi_bk.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
public class BulkOperationRequest {
    private List<Long> productIds;

    // Fields that can be updated in bulk
    private String status;
    private Boolean featured;
    private BigDecimal price; // For price adjustments
    private Double discountPercentage; // For applying percentage discounts
    private Integer stockQuantity; // For updating inventory
    private Long categoryId; // For changing category
    private Set<Long> addTagIds; // For adding tags
    private Set<Long> removeTagIds; // For removing tags

    // Operation type
    private OperationType operationType;

    // Price adjustment type
    private PriceAdjustmentType priceAdjustmentType;

    public enum OperationType {
        UPDATE,
        DELETE,
        PUBLISH,
        UNPUBLISH,
        ADJUST_PRICE,
        ADJUST_STOCK,
        CHANGE_CATEGORY,
        UPDATE_TAGS
    }

    public enum PriceAdjustmentType {
        FIXED, // Set to specific value
        INCREASE_AMOUNT, // Add fixed amount
        DECREASE_AMOUNT, // Subtract fixed amount
        INCREASE_PERCENT, // Increase by percentage
        DECREASE_PERCENT // Decrease by percentage
    }
}