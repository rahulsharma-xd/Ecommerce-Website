package com.ecommerce.app.service;

import com.ecommerce.app.entity.Order;
import com.ecommerce.app.entity.OrderItem;
import com.ecommerce.app.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");

    @Async
    public void sendOrderConfirmation(User user, Order order) {
        if (mailSender == null) {
            System.out.println("Mail disabled on Render - skipping email to: " + user.getEmail());
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(user.getEmail());
            helper.setSubject("Order Confirmation - " + order.getOrderNumber());
            helper.setText(buildOrderConfirmationEmail(user, order), true);

            mailSender.send(message);

            System.out.println("Order confirmation email sent to: " + user.getEmail());
        } catch (MessagingException e) {
            System.err.println("Failed to send order confirmation email: " + e.getMessage());
        } catch (MailException e) {
            System.err.println("Mail server error: " + e.getMessage());
        }
    }

    private String buildOrderConfirmationEmail(User user, Order order) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }");
        html.append(".content { padding: 20px; background-color: #f9f9f9; }");
        html.append(".order-details { background-color: white; padding: 15px; margin: 15px 0; border-radius: 5px; }");
        html.append(".items-table { width: 100%; border-collapse: collapse; margin: 15px 0; }");
        html.append(".items-table th { background-color: #f2f2f2; padding: 10px; text-align: left; border-bottom: 2px solid #ddd; }");
        html.append(".items-table td { padding: 10px; border-bottom: 1px solid #ddd; }");
        html.append(".total { font-size: 18px; font-weight: bold; text-align: right; margin-top: 15px; }");
        html.append(".footer { text-align: center; padding: 20px; color: #777; font-size: 12px; }");
        html.append("</style></head><body><div class='container'>");
        html.append("<div class='header'><h1>✓ Order Confirmed!</h1></div>");
        html.append("<div class='content'>");
        html.append("<p>Hi ").append(user.getUsername()).append(",</p>");
        html.append("<p>Thank you for your order! We're happy to confirm that we've received your order and it's being processed.</p>");
        html.append("<div class='order-details'>");
        html.append("<h3>Order Details</h3>");
        html.append("<p><strong>Order Number:</strong> ").append(order.getOrderNumber()).append("</p>");
        html.append("<p><strong>Order Date:</strong> ").append(order.getOrderedAt().format(DATE_FORMATTER)).append("</p>");
        html.append("<p><strong>Status:</strong> ").append(order.getStatus()).append("</p>");
        html.append("</div>");
        html.append("<h3>Order Items</h3><table class='items-table'>");
        html.append("<tr><th>Product</th><th>Quantity</th><th>Price</th><th>Subtotal</th></tr>");
        for (OrderItem item : order.getOrderItems()) {
            html.append("<tr>");
            html.append("<td>").append(item.getProduct().getName()).append("</td>");
            html.append("<td>").append(item.getQuantity()).append("</td>");
            html.append("<td>$").append(formatMoney(item.getPriceAtPurchase())).append("</td>");
            html.append("<td>$").append(formatMoney(item.getSubtotal())).append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");
        html.append("<div class='total'>Total: $").append(formatMoney(order.getTotalAmount())).append("</div>");
        html.append("<p>We'll send you another email when your order ships.</p>");
        html.append("</div>");
        html.append("<div class='footer'><p>Thank you for shopping with us!</p><p>&copy; 2025 E-commerce Store. All rights reserved.</p></div>");
        html.append("</div></body></html>");
        return html.toString();
    }

    private String formatMoney(BigDecimal amount) {
        return String.format("%.2f", amount);
    }
}
