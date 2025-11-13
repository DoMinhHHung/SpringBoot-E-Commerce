package iuh.fit.se.ecommerce.service.interfaces;

import iuh.fit.se.ecommerce.dto.request.PaymentRequest;
import iuh.fit.se.ecommerce.dto.request.PayOSWebhookRequest;
import iuh.fit.se.ecommerce.dto.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse createPayment(PaymentRequest request, String userEmail);
    void handlePayOSCallback(PayOSWebhookRequest webhook);
    PaymentResponse getPaymentStatus(Long orderCode);
}

