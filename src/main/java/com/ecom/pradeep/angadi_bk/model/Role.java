package com.ecom.pradeep.angadi_bk.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
//import org.springframework.data.annotation.Id;


@Entity
@Getter
@Setter
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private RoleType name;

    public enum RoleType {
        SUPER_ADMIN, ADMIN, STAFF, CUSTOMER, DELIVERY_PARTNER, DEVELOPER
    }
}