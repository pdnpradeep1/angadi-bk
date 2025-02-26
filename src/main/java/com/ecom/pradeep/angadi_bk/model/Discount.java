package com.ecom.pradeep.angadi_bk.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "discounts")
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;  // Unique discount code
    private BigDecimal discountAmount; // Flat discount amount
    private int percentage; // Percentage discount (0-100)
    private boolean active;
}
