package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.model.Order;
import com.ecom.pradeep.angadi_bk.service.OrderTrackingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders/tracking")
public class OrderTrackingController {
    private final OrderTrackingService orderTrackingService;

    public OrderTrackingController(OrderTrackingService orderTrackingService) {
        this.orderTrackingService = orderTrackingService;
    }

    @PutMapping("/{orderId}")
    public Order updateOrderStatus(@PathVariable Long orderId, @RequestParam String status) {
        return orderTrackingService.updateOrderStatus(orderId, status);
    }

    @GetMapping("/{orderId}")
    public Order getOrderStatus(@PathVariable Long orderId) {
        return orderTrackingService.getOrderStatus(orderId);
    }
}
