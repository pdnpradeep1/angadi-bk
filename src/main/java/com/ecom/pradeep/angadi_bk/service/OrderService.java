package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.model.Cart;
import com.ecom.pradeep.angadi_bk.model.Order;
import com.ecom.pradeep.angadi_bk.model.OrderItem;
import com.ecom.pradeep.angadi_bk.model.Product;
import com.ecom.pradeep.angadi_bk.repo.CartRepository;
import com.ecom.pradeep.angadi_bk.repo.OrderRepository;
import com.ecom.pradeep.angadi_bk.repo.ProductRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;
    private final SmsService smsService; // Optional SMS Service

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository, ProductRepository productRepository, EmailService emailService, SmsService smsService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.emailService = emailService;
        this.smsService = smsService;
    }

    public Order createOrder(Long customerId) {
        List<Cart> cartItems = cartRepository.findByCustomerId(customerId);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty. Add items before checkout.");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setCustomer(cartItems.get(0).getCustomer());
        order.setStatus("PENDING");

        for (Cart cartItem : cartItems) {
            Product product = cartItem.getProduct();

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Not enough stock for " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            totalAmount = totalAmount.add(orderItem.getPrice());
            order.getOrderItems().add(orderItem);
        }

        order.setTotalAmount(totalAmount.doubleValue());
        orderRepository.save(order);
        cartRepository.deleteAll(cartItems); // Clear cart after placing order


//         Send confirmation email to customer
        sendOrderConfirmationEmail(order);

        // Notify store owner
        notifyStoreOwner(order);
        return order;
    }


    private void sendOrderConfirmationEmail(Order order) {
        String subject = "Order Confirmation - #" + order.getOrderNumber();
        String body = "Dear " + order.getCustomer().getName() + ",\n\n" +
                "Your order #" + order.getOrderNumber() + " has been successfully placed!\n" +
                "We will notify you once it's shipped.\n\n" +
                "Thank you for shopping with us!";
        emailService.sendEmail(order.getCustomer().getEmail(), subject, body);

        // Optional SMS notification
//        smsService.sendSms(order.getCustomer().getPhoneNumber(), "Your order #" + order.getOrderNumber() + " is confirmed!");
    }

    private void notifyStoreOwner(Order order) {
        String storeOwnerEmail = order.getStore().getOwner().getEmail();
        String subject = "New Order Received - #" + order.getOrderNumber();
        String body = "Hello " + order.getStore().getOwner().getName() + ",\n\n" +
                "A new order #" + order.getOrderNumber() + " has been placed in your store.\n" +
                "Please review and process it.\n\nThanks!";
        emailService.sendEmail(storeOwnerEmail, subject, body);
    }
}
