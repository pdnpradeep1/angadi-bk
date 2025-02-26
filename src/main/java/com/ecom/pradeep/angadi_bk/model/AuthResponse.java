package com.ecom.pradeep.angadi_bk.model;

import lombok.Data;

import java.util.Set;

@Data
public class AuthResponse {
    private String token;
    private Set<Role> roles;

    public AuthResponse(String token, Set<Role> roles) {
        this.token = token;
        this.roles = roles;
    }

}
