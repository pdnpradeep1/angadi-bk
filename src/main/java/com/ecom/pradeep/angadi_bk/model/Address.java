package com.ecom.pradeep.angadi_bk.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    private String city;

    private String state;

    @Column(name = "postal_code")
    private String postalCode;

    private String country;

    private String phone;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}