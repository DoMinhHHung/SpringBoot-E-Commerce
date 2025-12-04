package iuh.fit.se.ecommerce.service.interfaces;

public interface EmailService {
    void sendEmail(String to, String subject, String body);

    void sendOrderStatusEmail(String toEmail, Long orderCode, String newStatus);
}
