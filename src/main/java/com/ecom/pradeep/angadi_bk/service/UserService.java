package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.exceptions.ResourceNotFoundException;
import com.ecom.pradeep.angadi_bk.model.Role;
import com.ecom.pradeep.angadi_bk.model.UpdateProfileRequest;
import com.ecom.pradeep.angadi_bk.model.User;
import com.ecom.pradeep.angadi_bk.repo.RoleRepository;
import com.ecom.pradeep.angadi_bk.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            Role defaultRole = roleRepository.findByName(Role.RoleType.CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Default role not found"));
            user.setRoles(Set.of(defaultRole));
        }
        return userRepository.save(user);
    }

    public User updateProfile(UpdateProfileRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
//        if (request.getPhone() != null) {
//            user.setPhone(request.getPhone());
//        }

        return userRepository.save(user);
    }


    public String updateProfilePicture(MultipartFile file) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String fileName = fileStorageService.saveFile(file, user.getEmail());
//        user.setProfilePicture(fileName); // TODO: Change the User model
        userRepository.save(user);

        return "Profile picture updated successfully!";
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}
