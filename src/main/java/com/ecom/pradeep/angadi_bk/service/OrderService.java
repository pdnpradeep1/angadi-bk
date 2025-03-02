package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.exceptions.ResourceNotFoundException;
import com.ecom.pradeep.angadi_bk.model.*;
import com.ecom.pradeep.angadi_bk.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final InventoryService inventoryService;
    private final StoreService storeService;

    @Autowired
    public OrderService(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            ProductRepository productRepository,
            StoreRepository storeRepository,
            UserRepository userRepository,
            EmailService emailService,
            SmsService smsService,
            InventoryService inventoryService,
            StoreService storeService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.smsService = smsService;
        this.inventoryService = inventoryService;
        this.storeService = storeService;
    }

    @Transactional
    public Order createOrder(Long customerId) {
        // Get cart items for the customer
        List<Cart> cartItems = cartRepository.findByCustomerId(customerId);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty. Add items before checkout.");
        }

        // Get customer and store from the first cart item
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Store store = cartItems.get(0).getProduct().getStore();

        // Create order with generated order number
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomer(customer);
        order.setStore(store);
        order.setStatus("PENDING");
        order.setPaymentStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        // Initialize totals
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal shipping = BigDecimal.valueOf(50); // Default shipping cost
        BigDecimal discount = BigDecimal.ZERO;

        // Create order items from cart items
        for (Cart cartItem : cartItems) {
            Product product = cartItem.getProduct();

            // Check if product has enough stock
            if (product.getStockQuantity() != -1 && product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Not enough stock for " + product.getName());
            }

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());

            // Calculate price (handle discounts if product has original price)
            BigDecimal itemPrice = product.getPrice();
            orderItem.setPrice(itemPrice);

            // Calculate item total
            orderItem.setTotal(itemPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            // Add to order items
            order.getOrderItems().add(orderItem);

            // Add to subtotal
            subtotal = subtotal.add(orderItem.getTotal());

            // Update inventory
            if (product.getStockQuantity() != -1) {
                product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
                productRepository.save(product);
            }
        }

        // Calculate tax (10% of subtotal)
        tax = subtotal.multiply(BigDecimal.valueOf(0.10));

        // Set order totals
        order.setSubtotal(subtotal);
        order.setShippingCost(shipping);
        order.setTax(tax);
        order.setDiscount(discount);

        // Calculate total amount
        BigDecimal totalAmount = subtotal.add(shipping).add(tax).subtract(discount);
        order.setTotalAmount(totalAmount.doubleValue());

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Clear the customer's cart
        cartRepository.deleteAll(cartItems);

        // Increment store order count
        storeService.incrementOrderCount(store.getId());

        // Send order confirmation
        sendOrderConfirmationEmail(savedOrder);

        return savedOrder;
    }

    private String generateOrderNumber() {
        // Generate a unique order number (prefix + random number)
        return "ORD-" + (1000 + new Random().nextInt(9000));
    }

    private void sendOrderConfirmationEmail(Order order) {
        String subject = "Order Confirmation - #" + order.getOrderNumber();
        String body = "Dear " + order.getCustomer().getName() + ",\n\n" +
                "Your order #" + order.getOrderNumber() + " has been successfully placed!\n" +
                "We will notify you once it's shipped.\n\n" +
                "Thank you for shopping with us!";
        emailService.sendEmail(order.getCustomer().getEmail(), subject, body);

        // Send SMS notification if phone number is available
        if (order.getCustomer().getPhone() != null) {
            smsService.sendSms(order.getCustomer().getPhone(),
                    "Your order #" + order.getOrderNumber() + " is confirmed!");
        }

        // Notify store owner
        String storeOwnerEmail = order.getStore().getOwner().getEmail();
        String ownerSubject = "New Order Received - #" + order.getOrderNumber();
        String ownerBody = "Hello " + order.getStore().getOwner().getName() + ",\n\n" +
                "A new order #" + order.getOrderNumber() + " has been placed in your store.\n" +
                "Please review and process it.\n\nThanks!";
        emailService.sendEmail(storeOwnerEmail, ownerSubject, ownerBody);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    @Transactional(readOnly = true)
    public Order getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
    }

    @Transactional(readOnly = true)
    public Page<Order> getOrdersByStore(Long storeId, Pageable pageable) {
        return orderRepository.findByStoreId(storeId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Order> getOrdersByStoreAndStatus(Long storeId, String status, Pageable pageable) {
        return orderRepository.findByStoreIdAndStatus(storeId, status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Order> searchOrders(Long storeId, String status, String keyword,
                                    LocalDateTime dateFrom, LocalDateTime dateTo,
                                    Double minAmount, Double maxAmount, String customerEmail,
                                    Pageable pageable) {

        Specification<Order> spec = Specification.where(null);

        // Add store filter
        spec = spec.and(OrderSpecification.hasStoreId(storeId));

        // Add status filter if provided
        if (status != null && !status.equalsIgnoreCase("all")) {
            spec = spec.and(OrderSpecification.hasStatus(status));
        }

        // Add keyword search if provided
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and(OrderSpecification.containsKeyword(keyword));
        }

        // Add date range filter if provided
        if (dateFrom != null) {
            spec = spec.and(OrderSpecification.createdAtGreaterThanOrEqual(dateFrom));
        }

        if (dateTo != null) {
            spec = spec.and(OrderSpecification.createdAtLessThanOrEqual(dateTo));
        }

        // Add amount range filter if provided
        if (minAmount != null) {
            spec = spec.and(OrderSpecification.totalAmountGreaterThanOrEqual(minAmount));
        }

        if (maxAmount != null) {
            spec = spec.and(OrderSpecification.totalAmountLessThanOrEqual(maxAmount));
        }

        // Add customer email filter if provided
        if (customerEmail != null && !customerEmail.isEmpty()) {
            spec = spec.and(OrderSpecification.hasCustomerEmail(customerEmail));
        }

        return orderRepository.findAll(spec, pageable);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = getOrderById(orderId);
        order.updateStatus(status);

        // If order is shipped, set tracking information
        if ("SHIPPED".equals(status)) {
            order.setTrackingNumber("TRK" + (10000000 + new Random().nextInt(90000000)) + "IN");
            order.setCarrierName("Express Delivery");
            order.setEstimatedDelivery(LocalDateTime.now().plusDays(3));

            // Send shipping notification
            String subject = "Order Shipped - #" + order.getOrderNumber();
            String body = "Dear " + order.getCustomer().getName() + ",\n\n" +
                    "Your order #" + order.getOrderNumber() + " has been shipped!\n" +
                    "Tracking number: " + order.getTrackingNumber() + "\n" +
                    "Carrier: " + order.getCarrierName() + "\n" +
                    "Estimated delivery: " + order.getEstimatedDelivery().toLocalDate() + "\n\n" +
                    "Thank you for shopping with us!";
            emailService.sendEmail(order.getCustomer().getEmail(), subject, body);
        }

        // If order is delivered, update payment status if pending
        if ("DELIVERED".equals(status) && "PENDING".equals(order.getPaymentStatus())) {
            order.setPaymentStatus("PAID");

            // Record revenue
            storeService.recordRevenue(order.getStore().getId(), BigDecimal.valueOf(order.getTotalAmount()));
        }

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getOrderStats(Long storeId) {
        Map<String, Long> stats = new HashMap<>();

        stats.put("total", orderRepository.countByStoreId(storeId));
        stats.put("pending", orderRepository.countByStoreIdAndStatus(storeId, "PENDING"));
        stats.put("processing", orderRepository.countByStoreIdAndStatus(storeId, "PROCESSING"));
        stats.put("shipped", orderRepository.countByStoreIdAndStatus(storeId, "SHIPPED"));
        stats.put("delivered", orderRepository.countByStoreIdAndStatus(storeId, "DELIVERED"));
        stats.put("cancelled", orderRepository.countByStoreIdAndStatus(storeId, "CANCELLED"));
        stats.put("refunded", orderRepository.countByStoreIdAndStatus(storeId, "REFUNDED"));
        stats.put("returned", orderRepository.countByStoreIdAndStatus(storeId, "RETURNED"));

        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getOrderStatsByTimeRange(Long storeId, String timeRange) {
        Map<String, Long> stats = new HashMap<>();
        LocalDateTime startDate = null;
        LocalDateTime endDate = LocalDateTime.now();

        switch (timeRange) {
            case "today":
                startDate = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                break;
            case "yesterday":
                startDate = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);
                endDate = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                break;
            case "7days":
                startDate = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIN);
                break;
            case "30days":
                startDate = LocalDateTime.of(LocalDate.now().minusDays(30), LocalTime.MIN);
                break;
            case "thisMonth":
                startDate = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
                break;
            case "lastMonth":
                LocalDate lastMonth = LocalDate.now().minusMonths(1);
                startDate = LocalDateTime.of(lastMonth.withDayOfMonth(1), LocalTime.MIN);
                endDate = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
                break;
            default:
                // Default to all time
                stats.put("total", orderRepository.countByStoreId(storeId));
                stats.put("pending", orderRepository.countByStoreIdAndStatus(storeId, "PENDING"));
                stats.put("processing", orderRepository.countByStoreIdAndStatus(storeId, "PROCESSING"));
                stats.put("shipped", orderRepository.countByStoreIdAndStatus(storeId, "SHIPPED"));
                stats.put("delivered", orderRepository.countByStoreIdAndStatus(storeId, "DELIVERED"));
                stats.put("cancelled", orderRepository.countByStoreIdAndStatus(storeId, "CANCELLED"));
                stats.put("refunded", orderRepository.countByStoreIdAndStatus(storeId, "REFUNDED"));
                stats.put("returned", orderRepository.countByStoreIdAndStatus(storeId, "RETURNED"));
                return stats;
        }

        List<Order> filteredOrders = orderRepository.findByStoreIdAndCreatedAtBetween(storeId, startDate, endDate);

        // Count orders by status
        Map<String, Long> statusCounts = filteredOrders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

        stats.put("total", (long) filteredOrders.size());
        stats.put("pending", statusCounts.getOrDefault("PENDING", 0L));
        stats.put("processing", statusCounts.getOrDefault("PROCESSING", 0L));
        stats.put("shipped", statusCounts.getOrDefault("SHIPPED", 0L));
        stats.put("delivered", statusCounts.getOrDefault("DELIVERED", 0L));
        stats.put("cancelled", statusCounts.getOrDefault("CANCELLED", 0L));
        stats.put("refunded", statusCounts.getOrDefault("REFUNDED", 0L));
        stats.put("returned", statusCounts.getOrDefault("RETURNED", 0L));

        return stats;
    }

    @Transactional
    public void sendOrderEmail(Long orderId, String emailType) {
        Order order = getOrderById(orderId);
        String subject = "";
        String body = "";

        switch (emailType) {
            case "order_confirmation":
                subject = "Order Confirmation - #" + order.getOrderNumber();
                body = "Dear " + order.getCustomer().getName() + ",\n\n" +
                        "Your order #" + order.getOrderNumber() + " has been successfully placed!\n" +
                        "We will notify you once it's shipped.\n\n" +
                        "Thank you for shopping with us!";
                break;

            case "shipping_confirmation":
                subject = "Order Shipped - #" + order.getOrderNumber();
                body = "Dear " + order.getCustomer().getName() + ",\n\n" +
                        "Your order #" + order.getOrderNumber() + " has been shipped!\n";

                if (order.getTrackingNumber() != null) {
                    body += "Tracking number: " + order.getTrackingNumber() + "\n" +
                            "Carrier: " + order.getCarrierName() + "\n";
                }

                if (order.getEstimatedDelivery() != null) {
                    body += "Estimated delivery: " + order.getEstimatedDelivery().toLocalDate() + "\n\n";
                }

                body += "Thank you for shopping with us!";
                break;

            case "invoice":
                subject = "Invoice for Order #" + order.getOrderNumber();
                body = "Dear " + order.getCustomer().getName() + ",\n\n" +
                        "Please find attached the invoice for your order #" + order.getOrderNumber() + ".\n\n" +
                        "Thank you for your business!";
                break;

            default:
                throw new IllegalArgumentException("Invalid email type: " + emailType);
        }

        emailService.sendEmail(order.getCustomer().getEmail(), subject, body);
    }
}