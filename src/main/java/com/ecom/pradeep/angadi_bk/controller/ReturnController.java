package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.model.Order;
import com.ecom.pradeep.angadi_bk.service.ReturnService;
import org.springframework.web.bind.annotation.*;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping("/returns")
public class ReturnController {
    private final ReturnService returnService;

    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    @PostMapping("/request/{orderId}")
    public Order requestReturn(@PathVariable Long orderId) {
        return returnService.requestReturn(orderId);
    }

    @PutMapping("/process/{orderId}")
    public Order processReturn(@PathVariable Long orderId, @RequestParam boolean refund) throws StripeException {
        return returnService.processReturn(orderId, refund);
    }
}
