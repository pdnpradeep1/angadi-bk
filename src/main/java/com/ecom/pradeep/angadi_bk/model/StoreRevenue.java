package com.ecom.pradeep.angadi_bk.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "store_revenue")
public class StoreRevenue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long storeId;
    private BigDecimal amount;
    private LocalDate date; // ✅ Track revenue date
}
