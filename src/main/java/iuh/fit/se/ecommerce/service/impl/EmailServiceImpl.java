package iuh.fit.se.ecommerce.service.impl;

import iuh.fit.se.ecommerce.service.interfaces.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Import thư viện SendGrid
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String defaultFromEmail;


    // Phương thức private để gửi mail qua SendGrid
    private void sendMailViaSendGrid(String toEmail, String subject, Content content) {
        // Tạo đối tượng Mail và From Email
        Email from = new Email(defaultFromEmail);
        Email to = new Email(toEmail);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            // Gửi request (Blocking call - cân nhắc dùng @Async nếu cần hiệu suất cao)
            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("SENDGRID SUCCESS: Email sent to {} with status {}", toEmail, response.getStatusCode());
            } else {
                log.error("SENDGRID ERROR: Failed to send email to {}. Status: {}, Body: {}",
                        toEmail, response.getStatusCode(), response.getBody());
                // Mày có thể ném ra Exception ở đây nếu muốn transaction rollback
            }
        } catch (Exception ex) {
            log.error("SENDGRID EXCEPTION: Failed to send email to {}: {}", toEmail, ex.getMessage());
        }
    }


    // 1. Triển khai phương thức gửi email cơ bản (body là text đơn thuần)
    @Override
    public void sendEmail(String to, String subject, String body) {
        Content content = new Content("text/plain", body);
        sendMailViaSendGrid(to, subject, content);
    }

    // 2. Triển khai phương thức gửi email cập nhật trạng thái đơn hàng (body là HTML)
    @Override
    public void sendOrderStatusEmail(String toEmail, Long orderCode, String newStatus) {
        String subject = "Cập nhật trạng thái đơn hàng #" + orderCode;
        String html = buildOrderStatusHtml(orderCode, newStatus);

        Content content = new Content("text/html", html);
        sendMailViaSendGrid(toEmail, subject, content);
    }

    // Giữ nguyên logic build HTML cũ
    private String buildOrderStatusHtml(Long orderCode, String newStatus) {
        return "<html><body>" +
                "<h3>Trạng thái đơn hàng cập nhật</h3>" +
                "<p>Đơn hàng <strong>#" + orderCode + "</strong> đã thay đổi trạng thái thành: <strong>" + newStatus + "</strong></p>" +
                "<p>Bạn có thể kiểm tra chi tiết đơn hàng trong tài khoản của mình.</p>" +
                "<p>Trân trọng,<br/>E-Commerce Team</p>" +
                "</body></html>";
    }

}
