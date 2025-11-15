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
import iuh.fit.se.ecommerce.service.interfaces.TransactionService;
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
    private final TransactionService transactionService;

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

        // Create transaction record
        transactionService.createPaymentTransaction(payment, user);

        log.info("Created payment for order: {}", order.getOrderCode());

        // Return response
        return PaymentResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .totalAmount(order.getTotalAmount())
                .qrCode(payOSResponse.getData().getQrCode()) // Trả về raw QR code data từ PayOS
                .checkoutUrl(payOSResponse.getData().getCheckoutUrl())
                .paymentLinkId(payOSResponse.getData().getPaymentLinkId())
                .status(PaymentStatus.PENDING)
                .message("Vui lòng quét mã QR hoặc click vào link để thanh toán")
                .build();
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

            // Update payment status
            if ("PAID".equals(webhook.getData().getCode()) || "00".equals(webhook.getData().getCode())) {
                payment.setStatus(PaymentStatus.PAID);
                String reference = webhook.getData().getReference();
                if (reference != null) {
                    payment.setTransactionId(reference);
                }
                // paidAt will be set by @UpdateTimestamp

                paymentRepository.save(payment);

                // Update transaction status to SUCCESS
                // Dùng reference từ webhook hoặc fallback sang paymentLinkId
                String externalTxId = reference != null 
                    ? reference 
                    : (webhook.getData().getPaymentLinkId() != null 
                        ? webhook.getData().getPaymentLinkId() 
                        : payment.getPaymentLinkId());
                transactionService.updateTransactionOnPaymentSuccess(payment, externalTxId);

                // Confirm order
                orderService.confirmOrder(orderCode);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                log.warn("Payment failed for order: {}", orderCode);
            }

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

        // Sync status from PayOS nếu payment vẫn đang PENDING
        if (payment.getStatus() == PaymentStatus.PENDING && payment.getPaymentLinkId() != null) {
            try {
                syncPaymentStatusFromPayOS(orderCode, payment);
                // Reload payment sau khi sync
                payment = paymentRepository.findByOrder(order)
                        .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Payment không tồn tại"));
            } catch (Exception e) {
                log.warn("Could not sync payment status from PayOS for order {}: {}", orderCode, e.getMessage());
            }
        }

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

    /**
     * Sync payment status from PayOS API
     * This method queries PayOS API to get the latest payment status and updates the database
     */
    @Transactional
    public void syncPaymentStatusFromPayOS(Long orderCode, Payment payment) {
        try {
            if (payment.getPaymentLinkId() == null) {
                log.warn("Payment link ID is null for order: {}, cannot sync status", orderCode);
                return;
            }

            log.info("Syncing payment status from PayOS for order: {}, paymentLinkId: {}", orderCode, payment.getPaymentLinkId());

            // Query status from PayOS
            PayOSGateway.PayOSResponse payOSResponse = payOSGateway.getPaymentStatus(payment.getPaymentLinkId());
            
            if (payOSResponse == null || payOSResponse.getData() == null) {
                log.warn("PayOS response is null or data is null for paymentLinkId: {}", payment.getPaymentLinkId());
                return;
            }

            // Check status from PayOS response
            // PayOS có thể trả về status trong data.code hoặc data.status
            String payOSStatus = payOSResponse.getData().getCode();
            if (payOSStatus == null || payOSStatus.isEmpty()) {
                payOSStatus = payOSResponse.getData().getStatus();
            }

            log.info("PayOS status for order {}: code={}, status={}", orderCode, payOSResponse.getCode(), payOSStatus);

            // Update payment status nếu PayOS báo đã thanh toán
            if ("PAID".equals(payOSStatus) || "00".equals(payOSStatus) || "PAID".equals(payOSResponse.getCode())) {
                if (payment.getStatus() != PaymentStatus.PAID) {
                    payment.setStatus(PaymentStatus.PAID);
                    if (payOSResponse.getData().getCode() != null) {
                        // Có thể có transaction reference trong response
                        // payment.setTransactionId(...);
                    }
                    paymentRepository.save(payment);
                    
                    // Update transaction status to SUCCESS
                    // PayOSResponse không có field reference, nên dùng transactionId từ payment hoặc paymentLinkId
                    String externalTxId = payment.getTransactionId() != null 
                        ? payment.getTransactionId() 
                        : (payOSResponse.getData().getPaymentLinkId() != null 
                            ? payOSResponse.getData().getPaymentLinkId() 
                            : payment.getPaymentLinkId());
                    transactionService.updateTransactionOnPaymentSuccess(payment, externalTxId);
                    
                    orderService.confirmOrder(orderCode);
                    log.info("✅ Payment status synced to PAID for order: {}", orderCode);
                } else {
                    log.debug("Payment already PAID for order: {}", orderCode);
                }
            } else if ("CANCELLED".equals(payOSStatus) || "CANCELLED".equals(payOSResponse.getCode())) {
                if (payment.getStatus() != PaymentStatus.CANCELLED) {
                    payment.setStatus(PaymentStatus.CANCELLED);
                    paymentRepository.save(payment);
                    log.info("Payment status synced to CANCELLED for order: {}", orderCode);
                }
            } else if ("FAILED".equals(payOSStatus) || "FAILED".equals(payOSResponse.getCode())) {
                if (payment.getStatus() != PaymentStatus.FAILED) {
                    payment.setStatus(PaymentStatus.FAILED);
                    paymentRepository.save(payment);
                    log.info("Payment status synced to FAILED for order: {}", orderCode);
                }
            } else {
                log.debug("Payment still PENDING on PayOS for order: {}, PayOS code: {}", orderCode, payOSResponse.getCode());
            }
        } catch (Exception e) {
            log.error("Error syncing payment status from PayOS for order {}: ", orderCode, e);
            // Không throw exception để không break polling
        }
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

