package com.ecom.pradeep.angadi_bk.model;

import com.ecom.pradeep.angadi_bk.model.InventoryTransaction;
import lombok.Data;

@Data
public class StockAdjustmentRequest {
    private Long productId;

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
}