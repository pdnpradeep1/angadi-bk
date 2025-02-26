package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.model.Order;
import com.ecom.pradeep.angadi_bk.repo.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.stripe.exception.StripeException;

import java.util.List;

@Service
public class ReturnService {
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final EmailService emailService;
    private final SmsService smsService;

    @Value("${return.reminder.interval}")
    private long reminderInterval;

    @Value("${return.reminder.maxAttempts}")
    private int maxReminderAttempts;


    public ReturnService(OrderRepository orderRepository, PaymentService paymentService, EmailService emailService, SmsService smsService) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
        this.emailService = emailService;
        this.smsService = smsService;
    }


    @Scheduled(fixedRate = 86400000) // Runs once every 24 hours
    public void sendPendingReturnReminders() {
        List<Order> pendingReturns = orderRepository.findByStatus("RETURN_REQUESTED");

        for (Order order : pendingReturns) {
            if (!order.canReceiveMoreReminders(maxReminderAttempts)) {
                continue; // Skip orders that have reached max reminders
            }
            String storeOwnerEmail = order.getStore().getOwner().getEmail();
//            String storeOwnerPhone = order.getStore().getOwner().getPhoneNumber();

            // Email Reminder
            String subject = "Pending Return Approval - Order #" + order.getOrderNumber();
            String body = "Hello " + order.getStore().getOwner().getName() + ",\n\n" +
                    "A return request for order #" + order.getOrderNumber() + " is still pending approval.\n" +
                    "Please review and process the request.\n\nThank you!";
            emailService.sendEmail(storeOwnerEmail, subject, body);

            // SMS Reminder
//            String smsMessage = "Reminder: Return request for Order #" + order.getOrderNumber() + " is still pending. Please review.";
//            smsService.sendSms(storeOwnerPhone, smsMessage);
            order.incrementReminderCount();
            orderRepository.save(order);
        }
    }

    public Order requestReturn(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found!"));

        if (!order.canBeReturned()) {
            throw new RuntimeException("Return not allowed.");
        }

        order.requestReturn();
        orderRepository.save(order);
        // Send return request notification
        sendReturnRequestNotification(order);
        return order;
    }

    public Order processReturn(Long orderId, boolean refund) throws StripeException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found!"));

        if (!"RETURN_REQUESTED".equals(order.getStatus())) {
            throw new RuntimeException("Return not approved.");
        }

        if (refund) {
            paymentService.processRefund(order.getOrderNumber());
            order.setStatus("REFUNDED");
        } else {
            order.setStatus("EXCHANGED");
        }

        orderRepository.save(order);

        // Send return completion notification
        sendReturnCompletionNotification(order, refund);

        return order;
    }

    private void sendReturnRequestNotification(Order order) {
        String customerEmail = order.getCustomer().getEmail();
        String storeOwnerEmail = order.getStore().getOwner().getEmail();

        // Email to customer
        String subjectCustomer = "Return Requested - Order #" + order.getOrderNumber();
        String bodyCustomer = "Dear " + order.getCustomer().getName() + ",\n\n" +
                "Your return request for order #" + order.getOrderNumber() + " has been received.\n" +
                "We will review your request and process it shortly.\n\n" +
                "Thank you!";
        emailService.sendEmail(customerEmail, subjectCustomer, bodyCustomer);

        // Email to store owner
        String subjectOwner = "Return Request for Order #" + order.getOrderNumber();
        String bodyOwner = "Hello " + order.getStore().getOwner().getName() + ",\n\n" +
                "A return request has been received for order #" + order.getOrderNumber() + ".\n" +
                "Please review and approve the request.\n\nThanks!";
        emailService.sendEmail(storeOwnerEmail, subjectOwner, bodyOwner);

        // Optional SMS to customer
//        smsService.sendSms(order.getCustomer().getPhoneNumber(),
//                "Your return request for order #" + order.getOrderNumber() + " has been received.");
    }

    private void sendReturnCompletionNotification(Order order, boolean refund) {
        String customerEmail = order.getCustomer().getEmail();
        String subject = refund ? "Refund Processed - Order #" + order.getOrderNumber() :
                "Exchange Processed - Order #" + order.getOrderNumber();

        String body = "Dear " + order.getCustomer().getName() + ",\n\n" +
                "Your return for order #" + order.getOrderNumber() + " has been " +
                (refund ? "refunded successfully." : "processed as an exchange.") +
                "\n\nThank you for shopping with us!";
        emailService.sendEmail(customerEmail, subject, body);

        // Optional SMS to customer
//        smsService.sendSms(order.getCustomer().getPhoneNumber(),
//                refund ? "Your refund for order #" + order.getOrderNumber() + " has been processed." :
//                        "Your exchange for order #" + order.getOrderNumber() + " is confirmed.");
    }
}

