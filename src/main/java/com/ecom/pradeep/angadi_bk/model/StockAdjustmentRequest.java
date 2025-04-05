package com.ecom.pradeep.angadi_bk.model;

import com.ecom.pradeep.angadi_bk.model.InventoryTransaction;
import lombok.Data;

@Data
public class StockAdjustmentRequest {
    // Store ID for the inventory update
    private Long storeId;
    
    // Product ID to update
    private Long productId;
    
    // Variant ID if updating a specific variantLowStockAlert
    // Add this field to your StockAdjustmentRequest class
    private Long variantId;
    
    // Add getter and setter
    public Long getVariantId() {
        return variantId;
    }
    
    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    // Can be positive (add stock) or negative (reduce stock)
    private int quantityChange;

    // Type of transaction
    private InventoryTransaction.TransactionType type;

    // Reason for adjustment
    private String reason;

    // Optional reference to an order
    private Long orderId;

    // Additional notes
    private String notes;
    
    // Default constructor with sensible defaults
    public StockAdjustmentRequest() {
        this.type = InventoryTransaction.TransactionType.ADJUSTMENT;
        this.reason = "Manual adjustment";
    }
    
    // Helper method to convert string type to enum
//    public void setType(String typeStr) {
//        if (typeStr == null) {
//            this.type = InventoryTransaction.TransactionType.ADJUSTMENT;
//            return;
//        }
//
//        try {
//            this.type = InventoryTransaction.TransactionType.valueOf(typeStr.toUpperCase());
//        } catch (IllegalArgumentException e) {
//            this.type = InventoryTransaction.TransactionType.ADJUSTMENT;
//        }
//    }
}