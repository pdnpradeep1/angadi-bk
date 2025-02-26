package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.model.Order;
import com.ecom.pradeep.angadi_bk.service.OrderCancellationService;
import org.springframework.web.bind.annotation.*;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping("/orders")
public class OrderCancellationController {
    private final OrderCancellationService orderCancellationService;

    public OrderCancellationController(OrderCancellationService orderCancellationService) {
        this.orderCancellationService = orderCancellationService;
    }

    @PutMapping("/cancel/{orderId}")
    public Order cancelOrder(@PathVariable Long orderId) throws StripeException {
        return orderCancellationService.cancelOrder(orderId);
    }
}
