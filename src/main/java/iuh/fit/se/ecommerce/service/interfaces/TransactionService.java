package iuh.fit.se.ecommerce.service.interfaces;

import iuh.fit.se.ecommerce.dto.response.TransactionResponse;
import iuh.fit.se.ecommerce.dto.response.TransactionSummaryResponse;
import iuh.fit.se.ecommerce.entity.Order;
import iuh.fit.se.ecommerce.entity.Payment;
import iuh.fit.se.ecommerce.entity.Transaction;
import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.entity.enums.TransactionStatus;
import iuh.fit.se.ecommerce.entity.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionService {
    /**
     * Tạo transaction khi thanh toán
     */
    Transaction createPaymentTransaction(Payment payment, User user);

    /**
     * Tạo transaction khi hoàn tiền
     */
    Transaction createRefundTransaction(Order order, BigDecimal amount, String reason);

    /**
     * Cập nhật status transaction
     */
    Transaction updateTransactionStatus(Long transactionId, TransactionStatus status);

    /**
     * Cập nhật transaction khi payment thành công
     */
    Transaction updateTransactionOnPaymentSuccess(Payment payment, String externalTransactionId);

    /**
     * Lấy transaction history của user (cho user)
     */
    Page<TransactionResponse> getUserTransactions(
            Long userId,
            TransactionType type,
            Pageable pageable
    );

    /**
     * Lấy tất cả transactions với filters (cho admin)
     */
    Page<TransactionResponse> getAllTransactions(
            TransactionType type,
            TransactionStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * Lấy transaction theo order
     */
    Page<TransactionResponse> getOrderTransactions(Long orderId, Pageable pageable);

    /**
     * Lấy transaction summary (cho admin)
     */
    TransactionSummaryResponse getTransactionSummary(
            TransactionType type,
            TransactionStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * Lấy transaction detail
     */
    TransactionResponse getTransactionDetail(Long transactionId);

    /**
     * Generate unique transaction code
     */
    String generateTransactionCode();
}

