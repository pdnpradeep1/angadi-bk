package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.model.EmailLog;
import com.ecom.pradeep.angadi_bk.model.Order;
import com.ecom.pradeep.angadi_bk.repo.EmailLogRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;
    private final EmailTemplateService emailTemplateService;

    public void sendResetPasswordEmail(String to, String resetToken) {
        String resetLink = "http://localhost:8080/auth/reset-password?token=" + resetToken;
        String subject = "Reset Your Password";
        String body = "<p>Click the link below to reset your password:</p>"
                + "<a href=\"" + resetLink + "\">Reset Password</a>";

        sendEmail(to, subject, body);
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);

            // Log email in the database
            EmailLog emailLog = new EmailLog(to, subject, body);
            emailLogRepository.save(emailLog);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendOrderConfirmationEmail(Order order) {
        String subject = "Order Confirmation - #" + order.getOrderNumber();
        String body = emailTemplateService.generateOrderConfirmationEmail(order);
        sendEmail(order.getCustomer().getEmail(), subject, body);
    }

    public void sendShippingConfirmationEmail(Order order) {
        String subject = "Your Order Has Been Shipped - #" + order.getOrderNumber();
        String body = emailTemplateService.generateShippingConfirmationEmail(order);
        sendEmail(order.getCustomer().getEmail(), subject, body);
    }

    public void sendInvoiceEmail(Order order) {
        String subject = "Invoice for Order #" + order.getOrderNumber();
        String body = emailTemplateService.generateInvoiceEmail(order);
        sendEmail(order.getCustomer().getEmail(), subject, body);
    }
}