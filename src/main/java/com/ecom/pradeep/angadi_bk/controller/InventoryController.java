package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.model.InventorySummary;
import com.ecom.pradeep.angadi_bk.model.InventoryTransaction;
import com.ecom.pradeep.angadi_bk.model.LowStockAlert;
import com.ecom.pradeep.angadi_bk.model.StockAdjustmentRequest;
import com.ecom.pradeep.angadi_bk.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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