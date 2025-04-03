package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.dto.InventoryTransactionDTO;
import com.ecom.pradeep.angadi_bk.model.InventoryTransaction;
import com.ecom.pradeep.angadi_bk.service.InventoryTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryTransactionController {

    private final InventoryTransactionService inventoryTransactionService;

    @Autowired
    public InventoryTransactionController(InventoryTransactionService inventoryTransactionService) {
        this.inventoryTransactionService = inventoryTransactionService;
    }

    @GetMapping("/transactions/product/{productId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<List<InventoryTransactionDTO>> getTransactionsByProduct(@PathVariable Long productId) {
        List<InventoryTransactionDTO> transactions = inventoryTransactionService.getTransactionsByProduct(productId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/store/{storeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<List<InventoryTransactionDTO>> getRecentTransactionsByStore(@PathVariable Long storeId) {
        List<InventoryTransactionDTO> transactions = inventoryTransactionService.getRecentTransactionsByStore(storeId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/store/{storeId}/type/{type}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<List<InventoryTransactionDTO>> getTransactionsByStoreAndType(
            @PathVariable Long storeId,
            @PathVariable String type) {
        try {
            InventoryTransaction.TransactionType transactionType = InventoryTransaction.TransactionType.valueOf(type.toUpperCase());
            List<InventoryTransactionDTO> transactions = 
                inventoryTransactionService.getTransactionsByStoreAndType(storeId, transactionType);
            return ResponseEntity.ok(transactions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/adjust/{productId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<?> adjustInventory(
            @PathVariable Long productId,
            @RequestBody Map<String, Object> request) {
        
        try {
            Integer quantity = Integer.parseInt(request.get("quantity").toString());
            String note = (String) request.get("note");
            String type = (String) request.get("type");
            
            InventoryTransaction.TransactionType transactionType = 
                InventoryTransaction.TransactionType.valueOf(type.toUpperCase());
            
            InventoryTransactionDTO transaction = 
                inventoryTransactionService.adjustInventory(productId, quantity, transactionType, note);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/transactions/product/{productId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<Void> deleteTransactionsByProduct(@PathVariable Long productId) {
        inventoryTransactionService.deleteTransactionsByProduct(productId);
        return ResponseEntity.noContent().build();
    }
}