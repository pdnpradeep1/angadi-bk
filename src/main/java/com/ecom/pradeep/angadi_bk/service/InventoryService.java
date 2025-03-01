package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.exceptions.ResourceNotFoundException;
import com.ecom.pradeep.angadi_bk.model.*;
import com.ecom.pradeep.angadi_bk.repo.InventoryTransactionRepository;
import com.ecom.pradeep.angadi_bk.repo.LowStockAlertRepository;
import com.ecom.pradeep.angadi_bk.repo.ProductRepository;
import com.ecom.pradeep.angadi_bk.repo.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final LowStockAlertRepository alertRepository;
    private final StoreRepository storeRepository;
    private final EmailService emailService;

    public InventoryService(
            ProductRepository productRepository,
            InventoryTransactionRepository transactionRepository,
            LowStockAlertRepository alertRepository,
            StoreRepository storeRepository,
            EmailService emailService) {
        this.productRepository = productRepository;
        this.transactionRepository = transactionRepository;
        this.alertRepository = alertRepository;
        this.storeRepository = storeRepository;
        this.emailService = emailService;
    }

    /**
     * Adjust stock level for a product and record the transaction
     *
     * @param request Stock adjustment request
     * @param ownerEmail Email of store owner for authorization
     * @return The created inventory transaction
     */
    @Transactional
    public InventoryTransaction adjustStock(StockAdjustmentRequest request, String ownerEmail) {
        // Get product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Check ownership
        if (!product.getStore().getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to adjust stock for this product");
        }

        // Handle unlimited inventory case
        if (product.getStockQuantity() == -1) {
            throw new RuntimeException("Cannot adjust stock for products with unlimited inventory");
        }

        // Calculate new stock level
        int oldQuantity = product.getStockQuantity();
        int newQuantity = oldQuantity + request.getQuantityChange();

        // Ensure stock doesn't go negative unless it's a special case
        if (newQuantity < 0 && request.getType() != InventoryTransaction.TransactionType.ADJUSTMENT) {
            throw new RuntimeException("Cannot reduce stock below zero. Current stock: " + oldQuantity);
        }

        // Update product stock
        product.setStockQuantity(newQuantity);
        productRepository.save(product);

        // Create inventory transaction
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(product);
        transaction.setQuantityChange(request.getQuantityChange());
        transaction.setRemainingQuantity(newQuantity);
        transaction.setType(request.getType());
        transaction.setReason(request.getReason());
        transaction.setPerformedBy(ownerEmail);
        transaction.setNotes(request.getNotes());

        if (request.getOrderId() != null) {
            // Optionally set order if this is related to an order
            // Requires OrderRepository and lookup logic
        }

        // Save transaction
        InventoryTransaction savedTransaction = transactionRepository.save(transaction);

        // Check for low stock threshold and create alert if needed
        checkLowStockThreshold(product);

        return savedTransaction;
    }

    /**
     * Reserve inventory for an order
     */
    @Transactional
    public void reserveInventory(Long productId, int quantity, Long orderId, String email) {
        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setProductId(productId);
        request.setQuantityChange(-quantity); // Negative change to reduce stock
        request.setType(InventoryTransaction.TransactionType.RESERVED);
        request.setReason("Reserved for order #" + orderId);
        request.setOrderId(orderId);

        adjustStock(request, email);
    }

    /**
     * Release reserved inventory (cancel reservation)
     */
    @Transactional
    public void releaseInventory(Long productId, int quantity, Long orderId, String email) {
        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setProductId(productId);
        request.setQuantityChange(quantity); // Positive change to increase stock
        request.setType(InventoryTransaction.TransactionType.UNRESERVED);
        request.setReason("Released reservation for order #" + orderId);
        request.setOrderId(orderId);

        adjustStock(request, email);
    }

    /**
     * Check if product is below low stock threshold and create alert if needed
     */
    private void checkLowStockThreshold(Product product) {
        // Skip unlimited inventory
        if (product.getStockQuantity() == -1) {
            return;
        }

        // Check if stock is below threshold
        if (product.getStockQuantity() > 0 &&
                product.getStockQuantity() <= product.getLowStockThreshold()) {

            // Check if an unacknowledged alert already exists
            boolean hasOpenAlert = alertRepository.existsByProductIdAndAcknowledgedFalse(product.getId());

            if (!hasOpenAlert) {
                // Create a new alert
                LowStockAlert alert = new LowStockAlert();
                alert.setProduct(product);
                alert.setCurrentStock(product.getStockQuantity());
                alert.setThresholdLevel(product.getLowStockThreshold());

                LowStockAlert savedAlert = alertRepository.save(alert);

                // Send notification
                sendLowStockNotification(savedAlert);
            }
        }
    }

    /**
     * Send email notification about low stock
     */
    private void sendLowStockNotification(LowStockAlert alert) {
        Product product = alert.getProduct();
        Store store = product.getStore();
        String ownerEmail = store.getOwner().getEmail();

        String subject = "Low Stock Alert: " + product.getName();
        String body = "Dear " + store.getOwner().getName() + ",\n\n" +
                "This is to inform you that the stock level for the following product is low:\n\n" +
                "Product: " + product.getName() + "\n" +
                "Current Stock: " + product.getStockQuantity() + "\n" +
                "Threshold Level: " + product.getLowStockThreshold() + "\n\n" +
                "Please consider restocking this item.\n\n" +
                "Regards,\n" +
                "Your Store Management System";

        emailService.sendEmail(ownerEmail, subject, body);
    }

    /**
     * Get inventory summary for a store
     */
    public InventorySummary getInventorySummary(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        List<Product> products = productRepository.findByStoreId(storeId);

        InventorySummary summary = new InventorySummary();
        summary.setTotalProducts(products.size());

        int inStockCount = 0;
        int outOfStockCount = 0;
        int lowStockCount = 0;

        for (Product product : products) {
            if (product.getStockQuantity() == -1) {
                // Unlimited stock
                inStockCount++;
            } else if (product.getStockQuantity() <= 0) {
                outOfStockCount++;
            } else if (product.getStockQuantity() <= product.getLowStockThreshold()) {
                lowStockCount++;
                inStockCount++;
            } else {
                inStockCount++;
            }
        }

        summary.setInStockCount(inStockCount);
        summary.setOutOfStockCount(outOfStockCount);
        summary.setLowStockCount(lowStockCount);

        summary.setLowStockProducts(
                products.stream()
                        .filter(p -> p.getStockQuantity() != -1 && p.getStockQuantity() > 0 && p.getStockQuantity() <= p.getLowStockThreshold())
                        .collect(Collectors.toList())
        );

        summary.setRecentTransactions(
                transactionRepository.findTop10ByProductStoreIdOrderByTimestampDesc(storeId)
        );

        return summary;
    }

    /**
     * Get low stock alerts for a store
     */
    public List<LowStockAlert> getLowStockAlerts(Long storeId, boolean includeAcknowledged) {
        if (includeAcknowledged) {
            return alertRepository.findByProductStoreIdOrderByCreatedAtDesc(storeId);
        } else {
            return alertRepository.findByProductStoreIdAndAcknowledgedFalseOrderByCreatedAtDesc(storeId);
        }
    }

    /**
     * Acknowledge a low stock alert
     */
    @Transactional
    public LowStockAlert acknowledgeAlert(Long alertId, String acknowledgedBy) {
        LowStockAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        alert.setAcknowledged(true);
        alert.setAcknowledgedAt(LocalDateTime.now());
        alert.setAcknowledgedBy(acknowledgedBy);

        return alertRepository.save(alert);
    }

    /**
     * Get transaction history for a product
     */
    public List<InventoryTransaction> getProductTransactionHistory(Long productId) {
        return transactionRepository.findByProductIdOrderByTimestampDesc(productId);
    }
}