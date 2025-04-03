package com.ecom.pradeep.angadi_bk.model;

import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
public class AuthResponse {
    private String token;
    private String refreshToken;
    private Long userId;
    private String email;
    private String name;
    private Set<Role> roles;

    // Constructor with all fields
    public AuthResponse(String token, String refreshToken, Long userId, String email, String name, Set<Role> roles) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.roles = roles;
    }

    // Original constructor for backward compatibility
    public AuthResponse(String token, Set<Role> roles) {
        this.token = token;
        this.roles = roles;
    }
}
