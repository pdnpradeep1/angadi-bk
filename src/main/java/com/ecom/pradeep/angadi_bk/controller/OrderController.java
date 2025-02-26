package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.model.Order;
import com.ecom.pradeep.angadi_bk.model.PaymentTransaction;
import com.ecom.pradeep.angadi_bk.service.PaymentService;
import com.ecom.pradeep.angadi_bk.service.OrderService;
import org.springframework.web.bind.annotation.*;
import com.stripe.exception.StripeException;
import java.math.BigDecimal;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final PaymentService paymentService;

    public OrderController(OrderService orderService, PaymentService paymentService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam Long customerId) throws StripeException {
        Order order = orderService.createOrder(customerId);
        return paymentService.createPaymentIntent(order);
    }

    @GetMapping("/status/{transactionId}")
    public String getTransactionStatus(@PathVariable String transactionId) {
        PaymentTransaction transaction = paymentService.getTransactionByTransactionId(transactionId);
        return transaction != null ? transaction.getPaymentStatus() : "NOT FOUND";
    }

    @PostMapping("/refund/{transactionId}")
    public String processRefund(@PathVariable String transactionId) throws StripeException {
        paymentService.processRefund(transactionId);
        return "Refund initiated successfully.";
    }
}
