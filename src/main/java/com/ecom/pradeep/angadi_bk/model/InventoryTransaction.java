package com.ecom.pradeep.angadi_bk.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

     @Enumerated(EnumType.STRING)
    private TransactionType type;
    
    private Integer quantity;
    
    // Added fields to match the service requirements
    private Integer quantityChange;
    private Integer remainingQuantity;
    private String reason;
    private String performedBy;
    private String note;
    
    private LocalDateTime timestamp = LocalDateTime.now();
    

    private Long orderId;
    
    public enum TransactionType {
        ADDITION,    // Adding new stock
        STOCK_REMOVAL,     // Removing stock (e.g., damaged goods)
        STOCK_ADJUSTMENT,  // Adjusting stock to a specific value
        ADJUSTMENT,        // Another form of adjustment
        SALE,              // Stock reduction due to sale
        RETURN,            // Stock increase due to return
        RESERVED,          // Stock reserved for an order
        UNRESERVED  ,       // Stock released from reservation
        INITIAL,
        PURCHASE
    }

    @Column(name = "variant_id")
    private Long variantId;

}