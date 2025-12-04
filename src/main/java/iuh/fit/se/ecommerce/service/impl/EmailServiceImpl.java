package iuh.fit.se.ecommerce.service.impl;

import iuh.fit.se.ecommerce.service.interfaces.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    @Override
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    @Override
    public void sendOrderStatusEmail(String toEmail, Long orderCode, String newStatus) {
        String subject = "Cập nhật trạng thái đơn hàng #" + orderCode;
        String html = buildOrderStatusHtml(orderCode, newStatus);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException ex) {
            System.err.println("Failed to send email to " + toEmail + ": " + ex.getMessage());
        }
    }

    private String buildOrderStatusHtml(Long orderCode, String newStatus) {
        return "<html><body>" +
                "<h3>Trạng thái đơn hàng cập nhật</h3>" +
                "<p>Đơn hàng <strong>#" + orderCode + "</strong> đã thay đổi trạng thái thành: <strong>" + newStatus + "</strong></p>" +
                "<p>Bạn có thể kiểm tra chi tiết đơn hàng trong tài khoản của mình.</p>" +
                "<p>Trân trọng,<br/>E-Commerce Team</p>" +
                "</body></html>";
    }

}
