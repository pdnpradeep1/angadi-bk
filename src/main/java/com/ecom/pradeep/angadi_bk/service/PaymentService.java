package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.model.Order;
import com.ecom.pradeep.angadi_bk.model.PaymentTransaction;
import com.ecom.pradeep.angadi_bk.repo.PaymentTransactionRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Map;

@Service
public class PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final EmailService emailService;


    public PaymentService(PaymentTransactionRepository paymentTransactionRepository, EmailService emailService) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.emailService = emailService;
    }

    public String createPaymentIntent(Order order) throws StripeException {
        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(BigDecimal.valueOf(order.getTotalAmount()).multiply(BigDecimal.valueOf(100)).longValue()) // Convert to cents
                        .setCurrency("usd")
                        .putMetadata("orderId", order.getId().toString())
//                        .setMetadata(Map.of("orderId", order.getId().toString()))
                        .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setTransactionId(paymentIntent.getId());
        transaction.setOrder(order);
        transaction.setAmount(BigDecimal.valueOf(order.getTotalAmount()));
        transaction.setPaymentStatus("PENDING");
        paymentTransactionRepository.save(transaction);

        return paymentIntent.getClientSecret();
    }

    public void updateTransactionStatus(String transactionId, String status) {
        PaymentTransaction transaction = paymentTransactionRepository.findByTransactionId(transactionId);
        if (transaction != null) {
            transaction.setPaymentStatus(status);
            paymentTransactionRepository.save(transaction);
        }
    }

    public void processRefund(String transactionId) throws StripeException {
        PaymentTransaction transaction = paymentTransactionRepository.findByTransactionId(transactionId);
        if (transaction == null || !"FAILED".equals(transaction.getPaymentStatus())) {
            throw new RuntimeException("Invalid transaction for refund.");
        }

        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(transactionId)
                .build();

        Refund refund = Refund.create(params);

        if ("succeeded".equals(refund.getStatus())) {
            transaction.setPaymentStatus("REFUNDED");
            paymentTransactionRepository.save(transaction);

//            Send refund notification email
            String subject = "Refund Processed for Your Order #" + transaction.getOrder().getOrderNumber();
            String body = "Dear " + transaction.getOrder().getCustomer().getName() + ",\n\n" +
                    "Your refund for Order #" + transaction.getOrder().getOrderNumber() +
                    " has been successfully processed. The amount of $" + transaction.getAmount() +
                    " will be refunded to your original payment method.\n\n" +
                    "Thank you for shopping with us!";
            emailService.sendEmail(transaction.getOrder().getCustomer().getEmail(), subject, body);
        }
    }

    public PaymentTransaction getTransactionByTransactionId(String transactionId) {
        return paymentTransactionRepository.findByTransactionId(transactionId);
    }
}
