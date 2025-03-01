package com.ecom.pradeep.angadi_bk.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "inventory_transactions")
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Change in quantity (positive for additions, negative for subtractions)
    private int quantityChange;

    // Remaining stock after this transaction
    private int remainingQuantity;

    // Reference to related order (if applicable)
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    // Type of transaction
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    // Reason for the adjustment
    private String reason;

    // Who performed the adjustment
    private String performedBy;

    // When the transaction occurred
    @CreationTimestamp
    private LocalDateTime timestamp;

    // Notes or additional details
    @Column(length = 500)
    private String notes;

    public enum TransactionType {
        PURCHASE,         // Buying inventory
        SALE,             // Selling product
        ADJUSTMENT,       // Manual adjustment
        RETURN,           // Customer return
        DAMAGED,          // Damaged goods
        TRANSFER,         // Transfer between locations
        INITIAL,          // Initial stock setting
        EXPIRED,          // Expired products
        RESERVED,         // Reserved for an order but not yet shipped
        UNRESERVED        // Released from reservation
    }
}