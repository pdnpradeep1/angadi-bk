package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class PasswordController {
    private final PasswordService passwordService;

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        try {
            String s = passwordService.forgotPassword(email);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.ok("failed");
        }
    }
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        return ResponseEntity.ok(passwordService.resetPassword(token, newPassword));
    }
}
