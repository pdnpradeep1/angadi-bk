package com.ecom.pradeep.angadi_bk.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Entity
@NoArgsConstructor // ✅ This ensures default constructor exists
@AllArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;

//    private LocalDateTime createdAt;

    private Date createdAt = new Date();

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;
    private double totalAmount;
    private String status; // PENDING, PAID, FAILED

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    public Order(OrderRequest orderRequest, User customer, Product product) {
        this.orderNumber = "ORD-" + System.currentTimeMillis(); // Generate a unique order number
//        this.createdAt = LocalDateTime.now();
        this.customer = customer;
        this.product = product;
        this.quantity = orderRequest.getQuantity();
        this.totalAmount = product.getPrice() * orderRequest.getQuantity();
    }
}
