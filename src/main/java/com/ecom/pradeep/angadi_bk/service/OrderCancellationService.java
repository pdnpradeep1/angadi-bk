package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.model.Order;
import com.ecom.pradeep.angadi_bk.repo.OrderRepository;
import org.springframework.stereotype.Service;
import com.stripe.exception.StripeException;

@Service
public class OrderCancellationService {
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final EmailService emailService;
    private final SmsService smsService;

    public OrderCancellationService(OrderRepository orderRepository, PaymentService paymentService, EmailService emailService, SmsService smsService) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
        this.emailService = emailService;
        this.smsService = smsService;
    }

    public Order cancelOrder(Long orderId) throws StripeException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found!"));

        if (!order.canBeCancelled()) {
            throw new RuntimeException("Order cannot be canceled at this stage.");
        }

        order.cancelOrder();
        orderRepository.save(order);

        // Process refund if order was prepaid
        if (order.isPrepaid()) {
            paymentService.processRefund(order.getOrderNumber());
        }


        // Send cancellation notification
        sendOrderCancellationNotification(order);

        return order;
    }

    private void sendOrderCancellationNotification(Order order) {
        String customerEmail = order.getCustomer().getEmail();
        String storeOwnerEmail = order.getStore().getOwner().getEmail();

        // Email to customer
        String subjectCustomer = "Order Canceled - #" + order.getOrderNumber();
        String bodyCustomer = "Dear " + order.getCustomer().getName() + ",\n\n" +
                "Your order #" + order.getOrderNumber() + " has been canceled.\n" +
                (order.isPrepaid() ? "A refund will be processed shortly." : "No payment was made.") +
                "\n\nThank you!";
        emailService.sendEmail(customerEmail, subjectCustomer, bodyCustomer);

        // Email to store owner
        String subjectOwner = "Order Canceled in Your Store - #" + order.getOrderNumber();
        String bodyOwner = "Hello " + order.getStore().getOwner().getName() + ",\n\n" +
                "Order #" + order.getOrderNumber() + " has been canceled by the customer.\n\n" +
                "Please take necessary actions.";
        emailService.sendEmail(storeOwnerEmail, subjectOwner, bodyOwner);

        // Optional: Send SMS to customer
//        smsService.sendSms(order.getCustomer().getPhoneNumber(),
//                "Your order #" + order.getOrderNumber() + " has been canceled. " +
//                        (order.isPrepaid() ? "Refund will be processed soon." : ""));
    }
}
