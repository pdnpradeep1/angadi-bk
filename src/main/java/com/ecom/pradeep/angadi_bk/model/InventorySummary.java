package com.ecom.pradeep.angadi_bk.model;

import com.ecom.pradeep.angadi_bk.model.InventoryTransaction;
import com.ecom.pradeep.angadi_bk.model.Product;
import lombok.Data;

import java.util.List;

@Data
public class InventorySummary {
    // Overall stats
    private int totalProducts;
    private int inStockCount;
    private int outOfStockCount;
    private int lowStockCount;

    // Lists for quick reference
    private List<Product> lowStockProducts;
    private List<InventoryTransaction> recentTransactions;
}