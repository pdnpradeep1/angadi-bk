package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.model.Order;
import com.ecom.pradeep.angadi_bk.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<Order> createOrder(@RequestParam Long customerId) {
        Order order = orderService.createOrder(customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<Order> getOrderByOrderNumber(@PathVariable String orderNumber) {
        Order order = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<Page<Order>> getOrdersByStore(
            @PathVariable Long storeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false) String customerEmail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        // Create pageable object with sorting
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        // Convert status parameter
        String statusFilter = status != null && !status.equalsIgnoreCase("all") ? status.toUpperCase() : null;

        // Search orders with filters
        Page<Order> orders = orderService.searchOrders(
                storeId, statusFilter, search, dateFrom, dateTo,
                minAmount, maxAmount, customerEmail, pageable);

        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request
    ) {
        String status = request.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().build();
        }

        Order updatedOrder = orderService.updateOrderStatus(orderId, status.toUpperCase());
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/stats/{storeId}")
    public ResponseEntity<Map<String, Long>> getOrderStats(
            @PathVariable Long storeId,
            @RequestParam(required = false) String timeRange
    ) {
        if (timeRange != null && !timeRange.isEmpty()) {
            return ResponseEntity.ok(orderService.getOrderStatsByTimeRange(storeId, timeRange));
        } else {
            return ResponseEntity.ok(orderService.getOrderStats(storeId));
        }
    }

    @PostMapping("/{orderId}/send-email")
    public ResponseEntity<String> sendOrderEmail(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request
    ) {
        String emailType = request.get("type");
        if (emailType == null) {
            return ResponseEntity.badRequest().body("Email type is required");
        }

        try {
            orderService.sendOrderEmail(orderId, emailType);
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send email: " + e.getMessage());
        }
    }
}