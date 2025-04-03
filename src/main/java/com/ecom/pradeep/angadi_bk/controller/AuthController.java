package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.model.AuthResponse;
import com.ecom.pradeep.angadi_bk.model.LoginRequest;
import com.ecom.pradeep.angadi_bk.model.RefreshToken;
import com.ecom.pradeep.angadi_bk.model.User;
import com.ecom.pradeep.angadi_bk.repo.RefreshTokenRepository;
import com.ecom.pradeep.angadi_bk.repo.UserRepository;
import com.ecom.pradeep.angadi_bk.service.RefreshTokenService;
import com.ecom.pradeep.angadi_bk.service.UserService;
import com.ecom.pradeep.angadi_bk.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        userService.registerUser(user);
        return ResponseEntity.ok("User registered successfully!");
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Find user by email
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

            // Verify password
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
            }

            // Set token expiration based on "Remember me" option
            long tokenExpirationMs = loginRequest.isRememberMe() 
                ? 24 * 60 * 60 * 1000L      // 24 hours even with "Remember me"
                : 1 * 60 * 60 * 1000L;      // 1 hour for regular login

            // Get user roles
            List<String> roles = user.getRoles().stream()
                    .map(role -> role.getName().toString()) // Convert RoleType to String
                    .collect(Collectors.toList());

            // Generate JWT token with the appropriate expiration
            String token = jwtUtil.generateToken(user.getEmail(), roles, tokenExpirationMs);
            
            // Delete any existing refresh tokens for this user before creating a new one
            refreshTokenRepository.deleteByUserId(user.getId());
            
            // Generate refresh token with expiration based on "Remember me"
            long refreshTokenExpirationMs = loginRequest.isRememberMe()
                ? 30 * 24 * 60 * 60 * 1000L  // 30 days for "Remember me"
                : 7 * 24 * 60 * 60 * 1000L;  // 7 days for regular login
                
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId(), refreshTokenExpirationMs);

            // Return the response
            return ResponseEntity.ok(new AuthResponse(
                token,
                refreshToken.getToken(),
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRoles()
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An error occurred during authentication: " + e.getMessage()));
        }
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshTokenService.validateRefreshToken(refreshToken)) {
            RefreshToken token = refreshTokenRepository.findByToken(refreshToken).get();
            List<String> roles = token.getUser().getRoles().stream()
                    .map(role -> role.getName().toString()) // Changed from name() to toString()
                    .collect(Collectors.toList());
            String newAccessToken = jwtUtil.generateToken(token.getUser().getEmail(), roles);

            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid refresh token");
    }

}
