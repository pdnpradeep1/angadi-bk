package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.model.InventorySummary;
import com.ecom.pradeep.angadi_bk.model.InventoryTransaction;
import com.ecom.pradeep.angadi_bk.model.LowStockAlert;
import com.ecom.pradeep.angadi_bk.model.StockAdjustmentRequest;
import com.ecom.pradeep.angadi_bk.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/adjust")
    public ResponseEntity<InventoryTransaction> adjustStock(
            @RequestBody StockAdjustmentRequest request,
            @RequestHeader("Owner-Email") String ownerEmail) {

        return ResponseEntity.ok(inventoryService.adjustStock(request, ownerEmail));
    }
    
    /**
     * New endpoint to handle inventory updates from the frontend
     * This matches the endpoint being called from the frontend: /inventory/{storeId}/update
     */
    @PostMapping("/{storeId}/update")
    public ResponseEntity<?> updateInventory(
            @PathVariable Long storeId,
            @RequestBody StockAdjustmentRequest request,
            @RequestHeader("Owner-Email") String ownerEmail) {
        
        // Set the storeId from the path parameter
        request.setStoreId(storeId);
        
        try {
            InventoryTransaction transaction = inventoryService.adjustStock(request, ownerEmail);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{storeId}/update/product/{productId}/variant/{variantId}")
    public ResponseEntity<?> updateVariantInventory(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @RequestBody StockAdjustmentRequest request,
            @RequestHeader("Owner-Email") String ownerEmail) {
        
        // Set the IDs from the path parameters
        request.setStoreId(storeId);
        request.setProductId(productId);
        request.setVariantId(variantId);
        
        try {
            InventoryTransaction transaction = inventoryService.adjustVariantStock(request, ownerEmail);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/summary/{storeId}")
    public ResponseEntity<InventorySummary> getInventorySummary(@PathVariable Long storeId) {
        return ResponseEntity.ok(inventoryService.getInventorySummary(storeId));
    }

    @GetMapping("/alerts/{storeId}")
    public ResponseEntity<List<LowStockAlert>> getLowStockAlerts(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "false") boolean includeAcknowledged) {

        return ResponseEntity.ok(inventoryService.getLowStockAlerts(storeId, includeAcknowledged));
    }

    @PostMapping("/alerts/{alertId}/acknowledge")
    public ResponseEntity<LowStockAlert> acknowledgeAlert(
            @PathVariable Long alertId,
            @RequestHeader("Owner-Email") String ownerEmail) {

        return ResponseEntity.ok(inventoryService.acknowledgeAlert(alertId, ownerEmail));
    }

    @GetMapping("/history/{productId}")
    public ResponseEntity<List<InventoryTransaction>> getProductTransactionHistory(
            @PathVariable Long productId) {

        return ResponseEntity.ok(inventoryService.getProductTransactionHistory(productId));
    }
}