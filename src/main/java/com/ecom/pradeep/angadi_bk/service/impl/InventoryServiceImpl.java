package com.ecom.pradeep.angadi_bk.service.impl;

import com.ecom.pradeep.angadi_bk.exceptions.ResourceNotFoundException;
import com.ecom.pradeep.angadi_bk.model.InventoryTransaction;
import com.ecom.pradeep.angadi_bk.model.Product;
import com.ecom.pradeep.angadi_bk.model.ProductVariant;
import com.ecom.pradeep.angadi_bk.model.StockAdjustmentRequest;
import com.ecom.pradeep.angadi_bk.repo.InventoryTransactionRepository;
import com.ecom.pradeep.angadi_bk.repo.ProductRepository;
import com.ecom.pradeep.angadi_bk.repo.ProductVariantRepository;
import com.ecom.pradeep.angadi_bk.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductVariantRepository productVariantRepository;
    
    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;
    
    @Autowired
    private InventoryService inventoryService;
    
    @Transactional
    public void adjustProductStock(Long productId, int quantityChange, String reason, String performedBy) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            
        List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
        
        if (!variants.isEmpty()) {
            // For products with variants, we need to distribute the change
            // This is a simple distribution - you might want a more sophisticated approach
            int variantCount = variants.size();
            int baseChange = quantityChange / variantCount;
            int remainder = quantityChange % variantCount;
            
            for (int i = 0; i < variants.size(); i++) {
                ProductVariant variant = variants.get(i);
                // Add extra unit to first variants if there's a remainder
                int variantChange = baseChange + (i < remainder ? 1 : 0);
                adjustVariantStock(variant, variantChange, reason, performedBy);
            }
            
            // Recalculate product stock based on variants
            recalculateProductStock(product);
        } else {
            // For products without variants, use the main InventoryService
            StockAdjustmentRequest request = new StockAdjustmentRequest();
            request.setProductId(productId);
            request.setQuantityChange(quantityChange);
            request.setType(quantityChange >= 0 ? 
                InventoryTransaction.TransactionType.ADDITION : 
                InventoryTransaction.TransactionType.STOCK_REMOVAL);
            request.setReason(reason);
            request.setNotes("Stock adjustment via InventoryServiceImpl");
            
            inventoryService.adjustStock(request, performedBy);
        }
    }
    
    @Transactional
    public void adjustVariantStock(ProductVariant variant, int quantityChange, String reason, String performedBy) {
        int currentStock = variant.getStockQuantity();
        int newStock = Math.max(0, currentStock + quantityChange);
        
        // Create transaction
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(variant.getProduct());
        transaction.setVariantId(variant.getId().getVariantId()); // Set the variant ID
        transaction.setQuantity(currentStock);
        transaction.setQuantityChange(quantityChange);
        transaction.setRemainingQuantity(newStock);
        transaction.setType(quantityChange >= 0 ? 
            InventoryTransaction.TransactionType.ADDITION : 
            InventoryTransaction.TransactionType.STOCK_REMOVAL);
        transaction.setReason(reason);
        transaction.setPerformedBy(performedBy);
        
        // Include variant details in notes
        String variantDetails = "Variant SKU: " + variant.getSku();
        if (variant.getAttributes() != null && !variant.getAttributes().isEmpty()) {
            variantDetails += ", Attributes: " + variant.getAttributes().toString();
        }
        transaction.setNote(variantDetails);
        transaction.setTimestamp(LocalDateTime.now());
        
        // Update variant and save transaction
        variant.setStockQuantity(newStock);
        productVariantRepository.save(variant);
        inventoryTransactionRepository.save(transaction);
        
        // Recalculate parent product stock
        recalculateProductStock(variant.getProduct());
    }
    
    @Transactional
    public Product recalculateProductStock(Product product) {
        List<ProductVariant> variants = productVariantRepository.findByProductId(product.getId());
        
        if (!variants.isEmpty()) {
            int totalStock = variants.stream()
                .mapToInt(ProductVariant::getStockQuantity)
                .sum();
            
            product.setStockQuantity(totalStock);
            return productRepository.save(product);
        }
        
        return product;
    }
    
    /**
     * Import initial stock for a product with variants
     * @param product The product to set initial stock for
     * @param variants List of variants if any
     * @param performedBy User performing the action
     */
    @Transactional
    public void importInitialStock(Product product, List<ProductVariant> variants, String performedBy) {
        if (variants != null && !variants.isEmpty()) {
            // For products with variants
            for (ProductVariant variant : variants) {
                // Create inventory transaction for each variant
                createVariantInventoryTransaction(variant, variant.getStockQuantity(), performedBy);
            }
            
            // Calculate total variant quantity
            int totalVariantQuantity = variants.stream()
                .mapToInt(ProductVariant::getStockQuantity)
                .sum();
            
            // Update product quantity to match total variant quantity
            product.setStockQuantity(totalVariantQuantity);
            productRepository.save(product);
            
            // Create a summary transaction for the product
            createProductSummaryTransaction(product, totalVariantQuantity, performedBy, variants);
        } else {
            // For products without variants
            createProductInventoryTransaction(product, product.getStockQuantity(), performedBy);
        }
    }
    
    private void createProductSummaryTransaction(Product product, int quantity, String performedBy, List<ProductVariant> variants) {
        if (quantity <= 0) {
            logger.info("Skipping inventory transaction for product {} as total variant quantity is {}", 
                product.getName(), quantity);
            return;
        }
        
        try {
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setProduct(product);
            transaction.setQuantity(quantity);
            transaction.setQuantityChange(quantity);
            transaction.setRemainingQuantity(quantity);
            transaction.setType(InventoryTransaction.TransactionType.ADJUSTMENT);
            transaction.setReason("Initial import - Product with variants");
            transaction.setPerformedBy(performedBy);
            
            // Add variant information
            StringBuilder variantInfo = new StringBuilder("Product has variants: ");
            for (int i = 0; i < variants.size(); i++) {
                ProductVariant variant = variants.get(i);
                if (i > 0) {
                    variantInfo.append(", ");
                }
                variantInfo.append(variant.getSku()).append(" (").append(variant.getStockQuantity()).append(" units)");
            }
            transaction.setNote(variantInfo.toString());
            transaction.setTimestamp(LocalDateTime.now());
            
            inventoryTransactionRepository.save(transaction);
            logger.info("Created summary inventory transaction for product {}: {} units (sum of variants)", 
                product.getName(), quantity);
        } catch (Exception e) {
            logger.error("Failed to create summary inventory transaction for product {}: {}", 
                product.getName(), e.getMessage(), e);
        }
    }
    
    private void createProductInventoryTransaction(Product product, int quantity, String performedBy) {
        if (quantity <= 0) {
            logger.info("Skipping inventory transaction for product {} as quantity is {}", 
                product.getName(), quantity);
            return;
        }
        
        try {
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setProduct(product);
            transaction.setQuantity(quantity);
            transaction.setQuantityChange(quantity);
            transaction.setRemainingQuantity(quantity);
            transaction.setType(InventoryTransaction.TransactionType.ADJUSTMENT);
            transaction.setReason("Initial import");
            transaction.setPerformedBy(performedBy);
            transaction.setNote("Product imported via CSV");
            transaction.setTimestamp(LocalDateTime.now());
            
            inventoryTransactionRepository.save(transaction);
            logger.info("Created inventory transaction for product {}: {} units", 
                product.getName(), quantity);
        } catch (Exception e) {
            logger.error("Failed to create inventory transaction for product {}: {}", 
                product.getName(), e.getMessage(), e);
        }
    }
    
    private void createVariantInventoryTransaction(ProductVariant variant, int quantity, String performedBy) {
        if (quantity <= 0) {
            logger.info("Skipping inventory transaction for variant {} as quantity is {}", 
                variant.getSku(), quantity);
            return;
        }
        
        try {
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setProduct(variant.getProduct());
            transaction.setQuantity(quantity);
            transaction.setQuantityChange(quantity);
            transaction.setRemainingQuantity(quantity);
            transaction.setType(InventoryTransaction.TransactionType.ADJUSTMENT);
            transaction.setReason("Initial import - variant");
            transaction.setPerformedBy(performedBy);
            
            // Include variant details in the notes
            String variantDetails = "Variant SKU: " + variant.getSku();
            if (variant.getAttributes() != null && !variant.getAttributes().isEmpty()) {
                variantDetails += ", Attributes: " + variant.getAttributes().toString();
            }
            transaction.setNote("Variant imported via CSV: " + variantDetails);
            
            transaction.setTimestamp(LocalDateTime.now());
            
            inventoryTransactionRepository.save(transaction);
            logger.info("Created inventory transaction for variant {}: {} units", 
                variant.getSku(), quantity);
        } catch (Exception e) {
            logger.error("Failed to create inventory transaction for variant {}: {}", 
                variant.getSku(), e.getMessage(), e);
        }
    }
}