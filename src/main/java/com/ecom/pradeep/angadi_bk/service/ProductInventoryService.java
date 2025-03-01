package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.exceptions.ResourceNotFoundException;
import com.ecom.pradeep.angadi_bk.model.Product;
import com.ecom.pradeep.angadi_bk.repo.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductInventoryService {
//    private final ProductRepository productRepository;
//    private final InventoryTransactionRepository inventoryTransactionRepository;
//
//    public ProductInventoryService(ProductRepository productRepository, InventoryTransactionRepository inventoryTransactionRepository) {
//        this.productRepository = productRepository;
//        this.inventoryTransactionRepository = inventoryTransactionRepository;
//    }
//
//    // Methods for updating inventory with audit trail
//    public void adjustStock(Long productId, int quantity, String reason, String performedBy) {
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
//
//        // Update product stock
//        product.setStockQuantity(product.getStockQuantity() + quantity);
//        productRepository.save(product);
//
//        // Log transaction
//        InventoryTransaction transaction = new InventoryTransaction();
//        transaction.setProduct(product);
//        transaction.setQuantityChange(quantity);
//        transaction.setReason(reason);
//        transaction.setPerformedBy(performedBy);
//        transaction.setTimestamp(new Date());
//        inventoryTransactionRepository.save(transaction);
//
//        // Check for low stock and send alerts if needed
//        if (product.getStockQuantity() <= product.getLowStockThreshold()) {
//            // Send low stock alert
//            // This would need an event system or direct notification
//        }
//    }
}