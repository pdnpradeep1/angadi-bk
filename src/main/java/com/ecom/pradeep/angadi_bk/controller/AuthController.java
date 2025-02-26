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
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name()) // Convert RoleType Enum to String
                .collect(Collectors.toList());

        String token = jwtUtil.generateToken(user.getEmail(),roles);
        return ResponseEntity.ok(new AuthResponse(token, user.getRoles()));
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshTokenService.validateRefreshToken(refreshToken)) {
            RefreshToken token = refreshTokenRepository.findByToken(refreshToken).get();
            List<String> roles = token.getUser().getRoles().stream()
                    .map(role -> role.getName().name()) // Convert RoleType Enum to String
                    .collect(Collectors.toList());
            String newAccessToken = jwtUtil.generateToken(token.getUser().getEmail(),roles);

            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid refresh token");
    }

}
