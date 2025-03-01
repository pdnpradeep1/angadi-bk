package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.model.Product;
import com.ecom.pradeep.angadi_bk.model.Store;
import com.ecom.pradeep.angadi_bk.repo.ProductRepository;
import com.ecom.pradeep.angadi_bk.repo.StoreRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryAlertScheduler {
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final EmailService emailService;

    public InventoryAlertScheduler(
            ProductRepository productRepository,
            StoreRepository storeRepository,
            EmailService emailService) {
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.emailService = emailService;
    }

    /**
     * Send daily inventory alerts at 8 AM
     */
    @Scheduled(cron = "0 0 8 * * ?") // 8 AM every day
    public void sendDailyInventoryAlerts() {
        // Get all stores
        List<Store> stores = storeRepository.findAll();

        for (Store store : stores) {
            // Get low stock products for this store
            List<Product> lowStockProducts = productRepository.findByStoreIdAndStockQuantityBetween(
                    store.getId(),
                    10, // Use a default threshold here
                    0  // Greater than 0
            );

            // Get out of stock products
            List<Product> outOfStockProducts = productRepository.findByStoreIdAndStockQuantityLessThanOrEqualTo(
                    store.getId(),
                    0
            );

            // Skip if no alerts needed
            if (lowStockProducts.isEmpty() && outOfStockProducts.isEmpty()) {
                continue;
            }

            // Send consolidated email
            sendInventoryAlertEmail(store, lowStockProducts, outOfStockProducts);
        }
    }

    private void sendInventoryAlertEmail(Store store, List<Product> lowStockProducts, List<Product> outOfStockProducts) {
        String ownerEmail = store.getOwner().getEmail();
        String subject = "Daily Inventory Alert for " + store.getName();

        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(store.getOwner().getName()).append(",\n\n");
        body.append("Here is your daily inventory status report for ").append(store.getName()).append(":\n\n");

        if (!lowStockProducts.isEmpty()) {
            body.append("LOW STOCK PRODUCTS (").append(lowStockProducts.size()).append("):\n");
            body.append("------------------------------------------\n");

            for (Product product : lowStockProducts) {
                body.append(product.getName())
                        .append(" - Current Stock: ").append(product.getStockQuantity())
                        .append(" (Threshold: ").append(product.getLowStockThreshold()).append(")\n");
            }

            body.append("\n");
        }

        if (!outOfStockProducts.isEmpty()) {
            body.append("OUT OF STOCK PRODUCTS (").append(outOfStockProducts.size()).append("):\n");
            body.append("------------------------------------------\n");

            for (Product product : outOfStockProducts) {
                body.append(product.getName()).append("\n");
            }

            body.append("\n");
        }

        body.append("Please take necessary actions to restock these items.\n\n");
        body.append("Regards,\nYour Store Management System");

        emailService.sendEmail(ownerEmail, subject, body.toString());
    }
}