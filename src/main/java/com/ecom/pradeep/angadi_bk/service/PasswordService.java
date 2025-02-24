package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.model.User;
import com.ecom.pradeep.angadi_bk.repo.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(System.currentTimeMillis() + 3600000); // 1 hour
        userRepository.save(user);

//      Send reset password email
        emailService.sendResetPasswordEmail(email, resetToken);
        return resetToken;
    }

    @Transactional
    public String resetPassword(String resetToken, String newPassword) {
        User user = userRepository.findByResetToken(resetToken)
                .orElseThrow(() -> new IllegalStateException("Invalid or expired token"));

        if (user.getResetTokenExpiry() < System.currentTimeMillis()) {
            throw new IllegalStateException("Token expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
        return "Password reset successful";
    }
}
