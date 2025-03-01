package com.ecom.pradeep.angadi_bk.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "low_stock_alerts")
public class LowStockAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Current stock level that triggered the alert
    private int currentStock;

    // Threshold level
    private int thresholdLevel;

    // When the alert was created
    @CreationTimestamp
    private LocalDateTime createdAt;

    // Has this alert been acknowledged?
    private boolean acknowledged;

    // When the alert was acknowledged (if applicable)
    private LocalDateTime acknowledgedAt;

    // Who acknowledged the alert
    private String acknowledgedBy;

    // Has action been taken to restock?
    private boolean actioned;

    // When action was taken
    private LocalDateTime actionedAt;

    // Details of the action taken
    private String actionDetails;
}