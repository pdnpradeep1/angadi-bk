package com.ecom.pradeep.angadi_bk.controller;
import com.ecom.pradeep.angadi_bk.service.ShippingService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/shipping")
public class ShippingController {
    private final ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @GetMapping("/track/{trackingNumber}")
    public Map<String, Object> getShippingStatus(@PathVariable String trackingNumber) {
        return shippingService.getShippingStatus(trackingNumber);
    }
}
