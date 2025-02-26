package com.ecom.pradeep.angadi_bk.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "stores")
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String address;
    private boolean visible = true;
    private int visitorCount = 0; // ✅ Track visitors
    private int orderCount = 0;   // ✅ Track orders
    private BigDecimal totalRevenue = BigDecimal.ZERO; // ✅ Track total revenue
    private String customDomain; // ✅ Custom domain support

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}
