package iuh.fit.se.ecommerce.dto.response;

import iuh.fit.se.ecommerce.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long orderId;
    private Long orderCode;           // PayOS order code
    private BigDecimal totalAmount;
    private String qrCode;            // QR code image (base64 hoặc URL)
    private String checkoutUrl;       // PayOS checkout URL
    private String paymentLinkId;     // PayOS payment link ID
    private PaymentStatus status;
    private String message;
}

