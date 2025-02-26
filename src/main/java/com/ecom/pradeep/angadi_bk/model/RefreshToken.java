package com.ecom.pradeep.angadi_bk.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
//import org.springframework.data.annotation.Id;

//import javax.persistence.*;
import java.time.Instant;

@Entity
@Getter
@Setter
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;
}
