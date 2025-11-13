package iuh.fit.se.ecommerce.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.se.ecommerce.dto.request.PaymentRequest;
import iuh.fit.se.ecommerce.dto.request.PayOSWebhookRequest;
import iuh.fit.se.ecommerce.dto.response.PaymentResponse;
import iuh.fit.se.ecommerce.entity.Order;
import iuh.fit.se.ecommerce.entity.Payment;
import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.entity.enums.PaymentStatus;
import iuh.fit.se.ecommerce.exception.AppException;
import iuh.fit.se.ecommerce.exception.ErrorCode;
import iuh.fit.se.ecommerce.repository.PaymentRepository;
import iuh.fit.se.ecommerce.repository.UserRepository;
import iuh.fit.se.ecommerce.service.interfaces.OrderService;
import iuh.fit.se.ecommerce.service.interfaces.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderService orderService;
    private final PayOSGateway payOSGateway;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request, String userEmail) {
        // Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Create order
        Order order = orderService.createOrder(request, user);

        // Create payment link via PayOS
        Long amountInVND = order.getTotalAmount().longValue(); // PayOS v2 nhận số tiền ở đơn vị VND

        String description = String.format("Don hang #%d", order.getOrderCode());

        PayOSGateway.PayOSResponse payOSResponse = payOSGateway.createPaymentLink(
                order.getOrderCode(),
                amountInVND,
                description
        );

        // Create payment record
        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .paymentLinkId(payOSResponse.getData().getPaymentLinkId())
                .qrCode(payOSResponse.getData().getQrCode())
                .checkoutUrl(payOSResponse.getData().getCheckoutUrl())
                .status(PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);

        log.info("Created payment for order: {}", order.getOrderCode());

        // Return response
        return PaymentResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .totalAmount(order.getTotalAmount())
                .qrCode(formatQrCode(payOSResponse.getData().getQrCode()))
                .checkoutUrl(payOSResponse.getData().getCheckoutUrl())
                .paymentLinkId(payOSResponse.getData().getPaymentLinkId())
                .status(PaymentStatus.PENDING)
                .message("Vui lòng quét mã QR hoặc click vào link để thanh toán")
                .build();
    }

    private String formatQrCode(String qrCode) {
        if (qrCode == null || qrCode.isEmpty()) {
            return null;
        }
        if (qrCode.startsWith("data:image")) {
            return qrCode;
        }
        if (!qrCode.startsWith("http")) {
            return "data:image/png;base64," + qrCode;
        }
        return qrCode;
    }

    @Override
    @Transactional
    public void handlePayOSCallback(PayOSWebhookRequest webhook) {
        try {
            log.info("Received PayOS webhook: {}", objectMapper.writeValueAsString(webhook));

            if (!"00".equals(webhook.getCode())) {
                log.warn("PayOS webhook error: {} - {}", webhook.getCode(), webhook.getDesc());
                return;
            }

            if (webhook.getData() == null) {
                log.warn("PayOS webhook data is null");
                return;
            }

            Long orderCode = Long.parseLong(webhook.getData().getOrderCode());

            // Get order
            Order order = orderService.getOrderByCode(orderCode);

            // Get payment
            Payment payment = paymentRepository.findByOrder(order)
                    .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Payment không tồn tại"));

            // Verify webhook signature (simplified - should verify checksum)
            // TODO: Implement proper checksum verification

            // Update payment status
            if ("PAID".equals(webhook.getData().getCode()) || "00".equals(webhook.getData().getCode())) {
                payment.setStatus(PaymentStatus.PAID);
                payment.setTransactionId(webhook.getData().getReference());
                // paidAt will be set by @UpdateTimestamp

                // Confirm order
                orderService.confirmOrder(orderCode);

                log.info("Payment confirmed for order: {}", orderCode);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                log.warn("Payment failed for order: {}", orderCode);
            }

            paymentRepository.save(payment);

        } catch (Exception e) {
            log.error("Error handling PayOS webhook: ", e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Lỗi xử lý webhook: " + e.getMessage());
        }
    }

    @Override
    public PaymentResponse getPaymentStatus(Long orderCode) {
        Order order = orderService.getOrderByCode(orderCode);
        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Payment không tồn tại"));

        return PaymentResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .totalAmount(order.getTotalAmount())
                .qrCode(payment.getQrCode())
                .checkoutUrl(payment.getCheckoutUrl())
                .paymentLinkId(payment.getPaymentLinkId())
                .status(payment.getStatus())
                .message(getStatusMessage(payment.getStatus()))
                .build();
    }

    private String getStatusMessage(PaymentStatus status) {
        return switch (status) {
            case PENDING -> "Đang chờ thanh toán";
            case PAID -> "Đã thanh toán thành công";
            case CANCELLED -> "Đã hủy thanh toán";
            case FAILED -> "Thanh toán thất bại";
            case REFUNDED -> "Đã hoàn tiền";
        };
    }
}

