package iuh.fit.se.ecommerce.service.impl;

import iuh.fit.se.ecommerce.repository.UserRepository;
import iuh.fit.se.ecommerce.service.interfaces.EmailService;
import iuh.fit.se.ecommerce.service.interfaces.NotificationService;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusPublisher {

    private final NotificationService notificationService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    public OrderStatusPublisher(NotificationService notificationService, EmailService emailService, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    public void publish(Long orderId, Long userId, String newStatus) {
        String title = "Order #" + orderId + " status updated";
        String message = "Your order #" + orderId + " is now: " + newStatus;
        notificationService.createAndPush(userId, title, message, "ORDER", orderId);

        try {
            userRepository.findById(userId).ifPresent(u -> {
                String email = u.getEmail();
                if (email != null && !email.isBlank()) {
                    emailService.sendOrderStatusEmail(email, orderId, newStatus);
                }
            });
        } catch (Exception ex) {
            // log but do not fail order processing
            System.err.println("Failed to send order status email: " + ex.getMessage());
        }
    }
}
