// src/main/java/com/ecom/pradeep/angadi_bk/service/EmailTemplateService.java
package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.model.Order;
import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {

    public String generateOrderConfirmationEmail(Order order) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html>");
        htmlBuilder.append("<html lang=\"en\">");
        htmlBuilder.append("<head>");
        htmlBuilder.append("<meta charset=\"UTF-8\">");
        htmlBuilder.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        htmlBuilder.append("<title>Order Confirmation</title>");
        htmlBuilder.append("<style>");
        htmlBuilder.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        htmlBuilder.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        htmlBuilder.append(".header { background-color: #4f46e5; color: white; padding: 20px; text-align: center; }");
        htmlBuilder.append(".content { padding: 20px; border: 1px solid #ddd; }");
        htmlBuilder.append(".footer { text-align: center; margin-top: 20px; font-size: 12px; color: #888; }");
        htmlBuilder.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        htmlBuilder.append("th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }");
        htmlBuilder.append("th { background-color: #f8f9fa; }");
        htmlBuilder.append(".total { font-weight: bold; }");
        htmlBuilder.append("</style>");
        htmlBuilder.append("</head>");
        htmlBuilder.append("<body>");
        htmlBuilder.append("<div class=\"container\">");

        // Header
        htmlBuilder.append("<div class=\"header\">");
        htmlBuilder.append("<h1>Order Confirmation</h1>");
        htmlBuilder.append("<p>Thank you for your order!</p>");
        htmlBuilder.append("</div>");

        // Content
        htmlBuilder.append("<div class=\"content\">");
        htmlBuilder.append("<h2>Dear ").append(order.getCustomer().getName()).append(",</h2>");
        htmlBuilder.append("<p>Your order has been successfully placed. Here are your order details:</p>");

        // Order details
        htmlBuilder.append("<p><strong>Order Number:</strong> ").append(order.getOrderNumber()).append("</p>");
        htmlBuilder.append("<p><strong>Order Date:</strong> ").append(order.getCreatedAt().toString()).append("</p>");

        // Order items
        htmlBuilder.append("<table>");
        htmlBuilder.append("<tr><th>Product</th><th>Quantity</th><th>Price</th><th>Total</th></tr>");

        order.getOrderItems().forEach(item -> {
            htmlBuilder.append("<tr>");
            htmlBuilder.append("<td>").append(item.getProduct().getName()).append("</td>");
            htmlBuilder.append("<td>").append(item.getQuantity()).append("</td>");
            htmlBuilder.append("<td>₹").append(item.getPrice()).append("</td>");
            htmlBuilder.append("<td>₹").append(item.getTotal()).append("</td>");
            htmlBuilder.append("</tr>");
        });

        // Order summary
        htmlBuilder.append("<tr><td colspan=\"3\" class=\"total\">Subtotal</td><td>₹").append(order.getSubtotal()).append("</td></tr>");
        htmlBuilder.append("<tr><td colspan=\"3\" class=\"total\">Shipping</td><td>₹").append(order.getShippingCost()).append("</td></tr>");
        htmlBuilder.append("<tr><td colspan=\"3\" class=\"total\">Tax</td><td>₹").append(order.getTax()).append("</td></tr>");

        if (order.getDiscount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            htmlBuilder.append("<tr><td colspan=\"3\" class=\"total\">Discount</td><td>-₹").append(order.getDiscount()).append("</td></tr>");
        }

        htmlBuilder.append("<tr><td colspan=\"3\" class=\"total\">Total</td><td>₹").append(order.getTotalAmount()).append("</td></tr>");
        htmlBuilder.append("</table>");

        // Shipping address
        htmlBuilder.append("<h3>Shipping Address</h3>");
        htmlBuilder.append("<p>");
        htmlBuilder.append(order.getShippingAddress().getAddressLine1()).append("<br>");
        if (order.getShippingAddress().getAddressLine2() != null && !order.getShippingAddress().getAddressLine2().isEmpty()) {
            htmlBuilder.append(order.getShippingAddress().getAddressLine2()).append("<br>");
        }
        htmlBuilder.append(order.getShippingAddress().getCity()).append(", ");
        htmlBuilder.append(order.getShippingAddress().getState()).append(" ");
        htmlBuilder.append(order.getShippingAddress().getPostalCode()).append("<br>");
        htmlBuilder.append(order.getShippingAddress().getCountry());
        htmlBuilder.append("</p>");

        htmlBuilder.append("<p>We will notify you once your order has been shipped.</p>");
        htmlBuilder.append("<p>Thank you for shopping with us!</p>");
        htmlBuilder.append("</div>");

        // Footer
        // Continuing from where we left off in EmailTemplateService.java
        htmlBuilder.append("<div class=\"footer\">");
        htmlBuilder.append("<p>© 2025 ").append(order.getStore().getName()).append(". All rights reserved.</p>");
        htmlBuilder.append("<p>If you have any questions about your order, please contact our customer service.</p>");
        htmlBuilder.append("</div>");

        htmlBuilder.append("</div>");
        htmlBuilder.append("</body>");
        htmlBuilder.append("</html>");

        return htmlBuilder.toString();
    }

    public String generateShippingConfirmationEmail(Order order) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html>");
        htmlBuilder.append("<html lang=\"en\">");
        htmlBuilder.append("<head>");
        htmlBuilder.append("<meta charset=\"UTF-8\">");
        htmlBuilder.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        htmlBuilder.append("<title>Shipping Confirmation</title>");
        htmlBuilder.append("<style>");
        htmlBuilder.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        htmlBuilder.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        htmlBuilder.append(".header { background-color: #4f46e5; color: white; padding: 20px; text-align: center; }");
        htmlBuilder.append(".content { padding: 20px; border: 1px solid #ddd; }");
        htmlBuilder.append(".footer { text-align: center; margin-top: 20px; font-size: 12px; color: #888; }");
        htmlBuilder.append(".tracking-box { background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0; }");
        htmlBuilder.append("</style>");
        htmlBuilder.append("</head>");
        htmlBuilder.append("<body>");
        htmlBuilder.append("<div class=\"container\">");

        // Header
        htmlBuilder.append("<div class=\"header\">");
        htmlBuilder.append("<h1>Your Order Has Been Shipped!</h1>");
        htmlBuilder.append("</div>");

        // Content
        htmlBuilder.append("<div class=\"content\">");
        htmlBuilder.append("<h2>Dear ").append(order.getCustomer().getName()).append(",</h2>");
        htmlBuilder.append("<p>We're excited to let you know that your order has been shipped and is on its way to you!</p>");

        // Order details
        htmlBuilder.append("<p><strong>Order Number:</strong> ").append(order.getOrderNumber()).append("</p>");

        // Tracking information
        htmlBuilder.append("<div class=\"tracking-box\">");
        htmlBuilder.append("<h3>Tracking Information</h3>");

        if (order.getTrackingNumber() != null) {
            htmlBuilder.append("<p><strong>Tracking Number:</strong> ").append(order.getTrackingNumber()).append("</p>");
        }

        if (order.getCarrierName() != null) {
            htmlBuilder.append("<p><strong>Carrier:</strong> ").append(order.getCarrierName()).append("</p>");
        }

        if (order.getEstimatedDelivery() != null) {
            htmlBuilder.append("<p><strong>Estimated Delivery Date:</strong> ").append(order.getEstimatedDelivery().toLocalDate()).append("</p>");
        }

        htmlBuilder.append("</div>");

        // Shipping address reminder
        htmlBuilder.append("<h3>Shipping Address</h3>");
        htmlBuilder.append("<p>");
        htmlBuilder.append(order.getShippingAddress().getAddressLine1()).append("<br>");
        if (order.getShippingAddress().getAddressLine2() != null && !order.getShippingAddress().getAddressLine2().isEmpty()) {
            htmlBuilder.append(order.getShippingAddress().getAddressLine2()).append("<br>");
        }
        htmlBuilder.append(order.getShippingAddress().getCity()).append(", ");
        htmlBuilder.append(order.getShippingAddress().getState()).append(" ");
        htmlBuilder.append(order.getShippingAddress().getPostalCode()).append("<br>");
        htmlBuilder.append(order.getShippingAddress().getCountry());
        htmlBuilder.append("</p>");

        htmlBuilder.append("<p>Thank you for shopping with us!</p>");
        htmlBuilder.append("</div>");

        // Footer
        htmlBuilder.append("<div class=\"footer\">");
        htmlBuilder.append("<p>© 2025 ").append(order.getStore().getName()).append(". All rights reserved.</p>");
        htmlBuilder.append("<p>If you have any questions about your order, please contact our customer service.</p>");
        htmlBuilder.append("</div>");

        htmlBuilder.append("</div>");
        htmlBuilder.append("</body>");
        htmlBuilder.append("</html>");

        return htmlBuilder.toString();
    }

    public String generateInvoiceEmail(Order order) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html>");
        htmlBuilder.append("<html lang=\"en\">");
        htmlBuilder.append("<head>");
        htmlBuilder.append("<meta charset=\"UTF-8\">");
        htmlBuilder.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        htmlBuilder.append("<title>Invoice</title>");
        htmlBuilder.append("<style>");
        htmlBuilder.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        htmlBuilder.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        htmlBuilder.append(".header { background-color: #4f46e5; color: white; padding: 20px; text-align: center; }");
        htmlBuilder.append(".content { padding: 20px; border: 1px solid #ddd; }");
        htmlBuilder.append(".footer { text-align: center; margin-top: 20px; font-size: 12px; color: #888; }");
        htmlBuilder.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        htmlBuilder.append("th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }");
        htmlBuilder.append("th { background-color: #f8f9fa; }");
        htmlBuilder.append(".total { font-weight: bold; }");
        htmlBuilder.append(".invoice-header { display: flex; justify-content: space-between; margin-bottom: 20px; }");
        htmlBuilder.append(".invoice-header div { width: 48%; }");
        htmlBuilder.append("</style>");
        htmlBuilder.append("</head>");
        htmlBuilder.append("<body>");
        htmlBuilder.append("<div class=\"container\">");

        // Header
        htmlBuilder.append("<div class=\"header\">");
        htmlBuilder.append("<h1>Invoice</h1>");
        htmlBuilder.append("<p>Order #").append(order.getOrderNumber()).append("</p>");
        htmlBuilder.append("</div>");

        // Content
        htmlBuilder.append("<div class=\"content\">");

        // Invoice header section with store and customer info
        htmlBuilder.append("<div class=\"invoice-header\">");

        // Store info
        htmlBuilder.append("<div>");
        htmlBuilder.append("<h3>").append(order.getStore().getName()).append("</h3>");
        htmlBuilder.append("<p>").append(order.getStore().getAddress()).append("</p>");
        htmlBuilder.append("</div>");

        // Customer info
        htmlBuilder.append("<div>");
        htmlBuilder.append("<h3>Bill To:</h3>");
        htmlBuilder.append("<p>").append(order.getCustomer().getName()).append("<br>");
        htmlBuilder.append(order.getCustomer().getEmail()).append("</p>");
        htmlBuilder.append("</div>");

        htmlBuilder.append("</div>");

        // Invoice details
        htmlBuilder.append("<p><strong>Invoice Date:</strong> ").append(order.getCreatedAt().toLocalDate()).append("</p>");
        htmlBuilder.append("<p><strong>Payment Status:</strong> ").append(order.getPaymentStatus()).append("</p>");

        // Order items
        htmlBuilder.append("<table>");
        htmlBuilder.append("<tr><th>Product</th><th>Quantity</th><th>Price</th><th>Total</th></tr>");

        order.getOrderItems().forEach(item -> {
            htmlBuilder.append("<tr>");
            htmlBuilder.append("<td>").append(item.getProduct().getName()).append("</td>");
            htmlBuilder.append("<td>").append(item.getQuantity()).append("</td>");
            htmlBuilder.append("<td>₹").append(item.getPrice()).append("</td>");
            htmlBuilder.append("<td>₹").append(item.getTotal()).append("</td>");
            htmlBuilder.append("</tr>");
        });

        // Order summary
        htmlBuilder.append("<tr><td colspan=\"3\" class=\"total\">Subtotal</td><td>₹").append(order.getSubtotal()).append("</td></tr>");
        htmlBuilder.append("<tr><td colspan=\"3\" class=\"total\">Shipping</td><td>₹").append(order.getShippingCost()).append("</td></tr>");
        htmlBuilder.append("<tr><td colspan=\"3\" class=\"total\">Tax</td><td>₹").append(order.getTax()).append("</td></tr>");

        if (order.getDiscount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            htmlBuilder.append("<tr><td colspan=\"3\" class=\"total\">Discount</td><td>-₹").append(order.getDiscount()).append("</td></tr>");
        }

        htmlBuilder.append("<tr><td colspan=\"3\" class=\"total\">Total</td><td>₹").append(order.getTotalAmount()).append("</td></tr>");
        htmlBuilder.append("</table>");

        htmlBuilder.append("<p>Thank you for your business!</p>");
        htmlBuilder.append("</div>");

        // Footer
        htmlBuilder.append("<div class=\"footer\">");
        htmlBuilder.append("<p>© 2025 ").append(order.getStore().getName()).append(". All rights reserved.</p>");
        htmlBuilder.append("</div>");

        htmlBuilder.append("</div>");
        htmlBuilder.append("</body>");
        htmlBuilder.append("</html>");

        return htmlBuilder.toString();
    }
}