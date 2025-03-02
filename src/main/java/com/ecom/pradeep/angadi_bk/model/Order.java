package com.ecom.pradeep.angadi_bk.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private User customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "store_id")
    private Store store;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "shipping_address_id")
    private Address shippingAddress;

    private String paymentMethod;

    private String paymentStatus;

    private BigDecimal subtotal;

    private BigDecimal shippingCost;

    private BigDecimal tax;

    private BigDecimal discount;

    private Double totalAmount;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String notes;

    private String trackingNumber;

    private String carrierName;

    private LocalDateTime estimatedDelivery;

    private boolean prepaid;

    private Integer reminderCount = 0;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Business methods
    public boolean canBeCancelled() {
        return "PENDING".equals(status) || "PROCESSING".equals(status);
    }

    public boolean canBeReturned() {
        return "DELIVERED".equals(status) &&
                createdAt.plusDays(30).isAfter(LocalDateTime.now());
    }

    public void cancelOrder() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled at this stage");
        }
        this.status = "CANCELLED";
    }

    public void requestReturn() {
        if (!canBeReturned()) {
            throw new IllegalStateException("Return not allowed for this order");
        }
        this.status = "RETURN_REQUESTED";
    }

    public void updateStatus(String newStatus) {
        // Validate status transition
        if ("CANCELLED".equals(newStatus) && !canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled at this stage");
        }

        if ("RETURN_REQUESTED".equals(newStatus) && !canBeReturned()) {
            throw new IllegalStateException("Return not allowed for this order");
        }

        this.status = newStatus;
    }

    public boolean canReceiveMoreReminders(int maxReminders) {
        return reminderCount < maxReminders;
    }

    public void incrementReminderCount() {
        this.reminderCount++;
    }

    public boolean isPrepaid() {
        return prepaid;
    }
}