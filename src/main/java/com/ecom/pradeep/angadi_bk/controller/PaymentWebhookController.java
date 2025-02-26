package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.service.PaymentService;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments/webhook")
public class PaymentWebhookController {
    private final PaymentService paymentService;

    public PaymentWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public void handleWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            String endpointSecret = "your_stripe_webhook_secret";
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

            if ("payment_intent.succeeded".equals(event.getType())) {
                PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().get();
                paymentService.updateTransactionStatus(paymentIntent.getId(), "SUCCESS");
            } else if ("payment_intent.payment_failed".equals(event.getType())) {
                PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().get();
                paymentService.processRefund(paymentIntent.getId());
                paymentService.updateTransactionStatus(paymentIntent.getId(), "FAILED");

            }
        } catch (Exception e) {
            System.out.println("Webhook error: " + e.getMessage());
        }
    }

}
