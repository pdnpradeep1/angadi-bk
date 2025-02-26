package com.ecom.pradeep.angadi_bk.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;
    private String paymentStatus; // SUCCESS, FAILED, PENDING
    private BigDecimal amount;
    private Date createdAt = new Date();

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}
