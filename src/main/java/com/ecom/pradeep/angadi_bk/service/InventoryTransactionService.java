package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.dto.InventoryTransactionDTO;
import com.ecom.pradeep.angadi_bk.exceptions.ResourceNotFoundException;
import com.ecom.pradeep.angadi_bk.model.InventoryTransaction;
import com.ecom.pradeep.angadi_bk.model.Product;
import com.ecom.pradeep.angadi_bk.repo.InventoryTransactionRepository;
import com.ecom.pradeep.angadi_bk.repo.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryTransactionService {

    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final ProductRepository productRepository;

    @Autowired
    public InventoryTransactionService(InventoryTransactionRepository inventoryTransactionRepository,
                                      ProductRepository productRepository) {
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.productRepository = productRepository;
    }

    /**
     * Get all inventory transactions for a product
     */
    public List<InventoryTransactionDTO> getTransactionsByProduct(Long productId) {
        List<InventoryTransaction> transactions = inventoryTransactionRepository.findByProductIdOrderByTimestampDesc(productId);
        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get recent inventory transactions for a store
     */
    public List<InventoryTransactionDTO> getRecentTransactionsByStore(Long storeId) {
        List<InventoryTransaction> transactions = inventoryTransactionRepository.findTop10ByProductStoreIdOrderByTimestampDesc(storeId);
        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get inventory transactions by store and type
     */
    public List<InventoryTransactionDTO> getTransactionsByStoreAndType(Long storeId, InventoryTransaction.TransactionType type) {
        List<InventoryTransaction> transactions = inventoryTransactionRepository.findByProductStoreIdAndTypeOrderByTimestampDesc(storeId, type);
        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Adjust inventory for a product
     */
    @Transactional
    public InventoryTransactionDTO adjustInventory(Long productId, Integer quantity, 
                                                 InventoryTransaction.TransactionType type, String note) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Create transaction record
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(product);
        transaction.setQuantity(quantity);
        transaction.setType(type);
        transaction.setNote(note);
        transaction.setTimestamp(LocalDateTime.now());

        // Update product stock based on transaction type
        int currentStock = (null != product.getStockQuantity()) ? product.getStockQuantity() : 0;
        
        switch (type) {
            case ADDITION:
                product.setStockQuantity(currentStock + quantity);
                break;
            case STOCK_REMOVAL:
                int newStock = currentStock - quantity;
                if (newStock < 0) {
                    throw new IllegalArgumentException("Cannot remove more stock than available");
                }
                product.setStockQuantity(newStock);
                break;
            case ADJUSTMENT:
                product.setStockQuantity(quantity); // Direct set to the new value
                break;
            case SALE:
                int afterSale = currentStock - quantity;
                if (afterSale < 0) {
                    throw new IllegalArgumentException("Cannot sell more stock than available");
                }
                product.setStockQuantity(afterSale);
                break;
            case RETURN:
                product.setStockQuantity(currentStock + quantity);
                break;
        }

        // Save product and transaction
        productRepository.save(product);
        InventoryTransaction savedTransaction = inventoryTransactionRepository.save(transaction);
        
        return convertToDTO(savedTransaction);
    }

    /**
     * Delete all transactions for a product
     */
    @Transactional
    public void deleteTransactionsByProduct(Long productId) {
        // Verify product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
        inventoryTransactionRepository.deleteByProductId(productId);
    }

    /**
     * Convert entity to DTO
     */
    private InventoryTransactionDTO convertToDTO(InventoryTransaction transaction) {
        InventoryTransactionDTO dto = new InventoryTransactionDTO();
        dto.setId(transaction.getId());
        dto.setProductId(transaction.getProduct().getId());
        dto.setProductName(transaction.getProduct().getName());
        dto.setQuantity(transaction.getQuantity());
        dto.setType(transaction.getType().name());
        dto.setNote(transaction.getNote());
        dto.setTimestamp(transaction.getTimestamp());
        return dto;
    }
}