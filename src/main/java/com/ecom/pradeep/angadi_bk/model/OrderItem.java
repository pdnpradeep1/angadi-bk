package com.ecom.pradeep.angadi_bk.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal total;

    @PrePersist
    protected void onCreate() {
        calculateTotal();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateTotal();
    }

    // Helper method to calculate the total
    private void calculateTotal() {
        if (price != null && quantity != null) {
            total = price.multiply(BigDecimal.valueOf(quantity));
        }
    }
}