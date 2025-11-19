package iuh.fit.se.ecommerce.service.impl;

import iuh.fit.se.ecommerce.dto.response.TransactionResponse;
import iuh.fit.se.ecommerce.dto.response.TransactionSummaryResponse;
import iuh.fit.se.ecommerce.entity.Order;
import iuh.fit.se.ecommerce.entity.Payment;
import iuh.fit.se.ecommerce.entity.Transaction;
import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.entity.enums.TransactionStatus;
import iuh.fit.se.ecommerce.entity.enums.TransactionType;
import iuh.fit.se.ecommerce.exception.AppException;
import iuh.fit.se.ecommerce.exception.ErrorCode;
import iuh.fit.se.ecommerce.repository.PaymentRepository;
import iuh.fit.se.ecommerce.repository.TransactionRepository;
import iuh.fit.se.ecommerce.repository.UserRepository;
import iuh.fit.se.ecommerce.service.interfaces.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public Transaction createPaymentTransaction(Payment payment, User user) {
        Transaction transaction = Transaction.builder()
                .user(user)
                .type(TransactionType.PAYMENT)
                .amount(payment.getAmount().negate())  // Số âm vì là chi ra
                .paymentMethod(payment.getMethod())
                .status(TransactionStatus.PENDING)
                .order(payment.getOrder())
                .payment(payment)
                .transactionCode(generateTransactionCode())
                .description(String.format("Thanh toán đơn hàng #%d", payment.getOrder().getOrderCode()))
                .build();

        transaction = transactionRepository.save(transaction);
        log.info("Created payment transaction: {} for order: {}", transaction.getTransactionCode(), payment.getOrder().getOrderCode());
        return transaction;
    }

    @Override
    @Transactional
    public Transaction createRefundTransaction(Order order, BigDecimal amount, String reason) {
        // Lấy payment từ order
        Optional<Payment> paymentOpt = paymentRepository.findByOrder(order);
        Payment payment = paymentOpt.orElse(null);
        
        Transaction transaction = Transaction.builder()
                .user(order.getUser())
                .type(TransactionType.REFUND)
                .amount(amount)
                .paymentMethod(payment != null ? payment.getMethod() : null)
                .status(TransactionStatus.SUCCESS)
                .order(order)
                .payment(payment)
                .transactionCode(generateTransactionCode())
                .description(String.format("Hoàn tiền đơn hàng #%d. Lý do: %s", order.getOrderCode(), reason))
                .completedAt(LocalDateTime.now())
                .build();

        transaction = transactionRepository.save(transaction);
        log.info("Created refund transaction: {} for order: {}", transaction.getTransactionCode(), order.getOrderCode());
        return transaction;
    }

    @Override
    @Transactional
    public Transaction updateTransactionStatus(Long transactionId, TransactionStatus status) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Transaction không tồn tại"));

        transaction.setStatus(status);
        if (status == TransactionStatus.SUCCESS) {
            transaction.setCompletedAt(LocalDateTime.now());
        }

        transaction = transactionRepository.save(transaction);
        log.info("Updated transaction {} status to {}", transaction.getTransactionCode(), status);
        return transaction;
    }

    @Override
    @Transactional
    public Transaction updateTransactionOnPaymentSuccess(Payment payment, String externalTransactionId) {
        Transaction transaction = transactionRepository.findByPayment(payment)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Transaction không tồn tại"));

        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setExternalTransactionId(externalTransactionId);
        transaction.setCompletedAt(LocalDateTime.now());

        transaction = transactionRepository.save(transaction);
        log.info("Updated transaction {} to SUCCESS for payment {}", transaction.getTransactionCode(), payment.getId());
        return transaction;
    }

    @Override
    public Page<TransactionResponse> getUserTransactions(Long userId, TransactionType type, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Page<Transaction> transactions;
        if (type != null) {
            transactions = transactionRepository.findByUserAndTypeOrderByCreatedAtDesc(user, type, pageable);
        } else {
            transactions = transactionRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        }

        return transactions.map(this::mapToResponse);
    }

    @Override
    public Page<TransactionResponse> getAllTransactions(
            TransactionType type,
            TransactionStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        // Nếu endDate được set, cần thêm 1 ngày để bao gồm cả ngày đó
        LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1) : null;

        Page<Transaction> transactions = transactionRepository.findAllWithFilters(
                type, status, startDate, endDateTime, pageable
        );

        return transactions.map(this::mapToResponse);
    }

    @Override
    public Page<TransactionResponse> getOrderTransactions(Long orderId, Pageable pageable) {
        // This would need OrderRepository, but for now return empty or implement later
        return Page.empty(pageable);
    }

    @Override
    public TransactionSummaryResponse getTransactionSummary(
            TransactionType type,
            TransactionStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        // Đếm số transaction (tất cả, không filter theo status nếu null)
        Long totalTransactions = status != null 
            ? transactionRepository.countByStatus(status)
            : transactionRepository.count();
        Long pendingTransactions = transactionRepository.countByStatus(TransactionStatus.PENDING);

        // Tính tổng revenue (chỉ lấy REFUND, DEPOSIT - số dương)
        BigDecimal totalRevenue = BigDecimal.ZERO;
        if (type == null || type == TransactionType.REFUND) {
            BigDecimal refundAmount = transactionRepository.sumAmountByFilters(
                    TransactionType.REFUND,
                    status != null ? status : TransactionStatus.SUCCESS
            );
            // REFUND là số dương
            totalRevenue = totalRevenue.add(refundAmount.max(BigDecimal.ZERO));
        }
        if (type == null || type == TransactionType.DEPOSIT) {
            BigDecimal depositAmount = transactionRepository.sumAmountByFilters(
                    TransactionType.DEPOSIT,
                    status != null ? status : TransactionStatus.SUCCESS
            );
            // DEPOSIT là số dương
            totalRevenue = totalRevenue.add(depositAmount.max(BigDecimal.ZERO));
        }

        // Tính tổng expense (chỉ lấy PAYMENT, WITHDRAWAL - số âm, nhưng lưu dương)
        BigDecimal totalExpense = BigDecimal.ZERO;
        if (type == null || type == TransactionType.PAYMENT) {
            BigDecimal paymentAmount = transactionRepository.sumAmountByFilters(
                    TransactionType.PAYMENT,
                    status != null ? status : TransactionStatus.SUCCESS
            );
            // Payment amount là số âm, nên cần abs để lấy giá trị dương
            totalExpense = totalExpense.add(paymentAmount.abs());
        }
        if (type == null || type == TransactionType.WITHDRAWAL) {
            BigDecimal withdrawalAmount = transactionRepository.sumAmountByFilters(
                    TransactionType.WITHDRAWAL,
                    status != null ? status : TransactionStatus.SUCCESS
            );
            // WITHDRAWAL là số âm, nên cần abs để lấy giá trị dương
            totalExpense = totalExpense.add(withdrawalAmount.abs());
        }

        // Tính net amount (revenue - expense)
        BigDecimal netAmount = totalRevenue.subtract(totalExpense);

        return TransactionSummaryResponse.builder()
                .totalTransactions(totalTransactions)
                .pendingTransactions(pendingTransactions)
                .totalRevenue(totalRevenue)
                .totalExpense(totalExpense)
                .netAmount(netAmount)
                .build();
    }

    @Override
    public TransactionResponse getTransactionDetail(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Transaction không tồn tại"));

        return mapToResponse(transaction);
    }

    @Override
    public String generateTransactionCode() {
        long timestamp = Instant.now().toEpochMilli();
        // Format: TXN + timestamp (last 12 digits)
        return "TXN" + (timestamp % 1000000000000L);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionCode(transaction.getTransactionCode())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .paymentMethod(transaction.getPaymentMethod())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .orderId(transaction.getOrder() != null ? transaction.getOrder().getId() : null)
                .orderCode(transaction.getOrder() != null ? transaction.getOrder().getOrderCode() : null)
                .paymentId(transaction.getPayment() != null ? transaction.getPayment().getId() : null)
                .externalTransactionId(transaction.getExternalTransactionId())
                .userName(transaction.getUser().getFullName())
                .userEmail(transaction.getUser().getEmail())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .build();
    }
}

