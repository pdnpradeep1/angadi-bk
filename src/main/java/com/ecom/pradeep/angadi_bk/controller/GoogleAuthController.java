package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.dto.GoogleTokenRequest;
import com.ecom.pradeep.angadi_bk.model.AuthResponse;
import com.ecom.pradeep.angadi_bk.service.GoogleAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class GoogleAuthController {

    @Autowired
    private GoogleAuthService googleAuthService;

    @PostMapping("/google-login")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleTokenRequest tokenRequest) {
        AuthResponse authResponse = googleAuthService.authenticateWithGoogle(tokenRequest.getToken());
        return ResponseEntity.ok(authResponse);
    }
}