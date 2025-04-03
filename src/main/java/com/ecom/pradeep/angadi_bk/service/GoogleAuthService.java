package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.model.AuthResponse;
import com.ecom.pradeep.angadi_bk.model.Role;
import com.ecom.pradeep.angadi_bk.model.User;
import com.ecom.pradeep.angadi_bk.repo.RoleRepository;
import com.ecom.pradeep.angadi_bk.repo.UserRepository;
import com.ecom.pradeep.angadi_bk.utils.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GoogleAuthService {

    @Value("${google.client.id}")
    private String googleClientId;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public AuthResponse authenticateWithGoogle(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                Payload payload = idToken.getPayload();

                // Get profile information from payload
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");
                
                // Check if user exists
                Optional<User> existingUserOpt = userRepository.findByEmail(email);
                User user;
                
                if (existingUserOpt.isPresent()) {
                    // Update existing user if needed
                    user = existingUserOpt.get();
                    // You might want to update user details here if needed
                } else {
                    // Create new user with CUSTOMER role
                    user = new User();
                    user.setEmail(email);
                    user.setName(name);
                    user.setProvider("GOOGLE");
                    user.setPassword(passwordEncoder.encode("Password@123"));
                    
                    // Set default CUSTOMER role
                    Role customerRole = roleRepository.findByName(Role.RoleType.CUSTOMER)
                            .orElseThrow(() -> new RuntimeException("Default role not found"));
                    Set<Role> roles = new HashSet<>();
                    roles.add(customerRole);
                    user.setRoles(roles);
                    
                    userRepository.save(user);
                }
                
                // Get roles for token generation
                List<String> roles = user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList());
                
                // Generate JWT token
                String token = jwtUtil.generateToken(user.getEmail(), roles);
                
                return new AuthResponse(token, user.getRoles());
            }
            throw new RuntimeException("Invalid ID token.");
        } catch (Exception e) {
            throw new RuntimeException("Error authenticating with Google: " + e.getMessage(), e);
        }
    }
}