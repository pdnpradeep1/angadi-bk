package com.ecom.pradeep.angadi_bk.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/secure")
public class SecureController {

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String adminAccess() {
        return "Admin Dashboard";
    }

    @GetMapping("/super-admin")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public String superAdminAccess() {
        return "Super Admin Panel";
    }
}
