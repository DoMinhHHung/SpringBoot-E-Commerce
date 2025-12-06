package iuh.fit.se.ecommerce.service.impl;

import iuh.fit.se.ecommerce.dto.response.TransactionResponse;
import iuh.fit.se.ecommerce.dto.response.TransactionSummaryResponse;
import iuh.fit.se.ecommerce.entity.Order;
import iuh.fit.se.ecommerce.entity.Payment;
import iuh.fit.se.ecommerce.entity.Transaction;
import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.entity.enums.PaymentMethod;
import iuh.fit.se.ecommerce.entity.enums.TransactionStatus;
import iuh.fit.se.ecommerce.entity.enums.TransactionType;
import iuh.fit.se.ecommerce.exception.AppException;
import iuh.fit.se.ecommerce.exception.ErrorCode;
import iuh.fit.se.ecommerce.repository.PaymentRepository;
import iuh.fit.se.ecommerce.repository.TransactionRepository;
import iuh.fit.se.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User testUser;
    private Order testOrder;
    private Payment testPayment;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .build();

        testOrder = Order.builder()
                .id(1L)
                .orderCode(1001L)
                .user(testUser)
                .build();

        testPayment = Payment.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100.00))
                .method(PaymentMethod.BANK_TRANSFER)
                .order(testOrder)
                .build();

        testTransaction = Transaction.builder()
                .id(1L)
                .user(testUser)
                .type(TransactionType.PAYMENT)
                .amount(BigDecimal.valueOf(-100.00))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .status(TransactionStatus.PENDING)
                .order(testOrder)
                .payment(testPayment)
                .transactionCode("TXN123456789")
                .description("Payment for order #1001")
                .build();
    }

    @Test
    void createPaymentTransaction_Success() {
        // Given
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // When
        Transaction result = transactionService.createPaymentTransaction(testPayment, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(TransactionType.PAYMENT);
        assertThat(result.getAmount()).isNegative(); // Payment is negative
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.BANK_TRANSFER);
        assertThat(result.getTransactionCode()).isNotNull();
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createRefundTransaction_Success() {
        // Given
        when(paymentRepository.findByOrder(testOrder)).thenReturn(Optional.of(testPayment));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        // When
        Transaction result = transactionService.createRefundTransaction(
                testOrder, 
                BigDecimal.valueOf(50.00), 
                "Order cancelled"
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(TransactionType.REFUND);
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(result.getCompletedAt()).isNotNull();
        assertThat(result.getDescription()).contains("Hoàn tiền").contains("Order cancelled");
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createRefundTransaction_NoPayment_Success() {
        // Given
        when(paymentRepository.findByOrder(testOrder)).thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        // When
        Transaction result = transactionService.createRefundTransaction(
                testOrder, 
                BigDecimal.valueOf(50.00), 
                "Order cancelled"
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPaymentMethod()).isNull();
        assertThat(result.getPayment()).isNull();
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void updateTransactionStatus_ToSuccess_Success() {
        // Given
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Transaction result = transactionService.updateTransactionStatus(1L, TransactionStatus.SUCCESS);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(result.getCompletedAt()).isNotNull();
        verify(transactionRepository).save(testTransaction);
    }

    @Test
    void updateTransactionStatus_ToFailed_Success() {
        // Given
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Transaction result = transactionService.updateTransactionStatus(1L, TransactionStatus.FAILED);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.FAILED);
        assertThat(result.getCompletedAt()).isNull(); // Only set for SUCCESS
        verify(transactionRepository).save(testTransaction);
    }

    @Test
    void updateTransactionStatus_TransactionNotFound_ThrowsException() {
        // Given
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.updateTransactionStatus(999L, TransactionStatus.SUCCESS))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Transaction không tồn tại");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void updateTransactionOnPaymentSuccess_Success() {
        // Given
        when(transactionRepository.findByPayment(testPayment)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Transaction result = transactionService.updateTransactionOnPaymentSuccess(testPayment, "EXT123456");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(result.getExternalTransactionId()).isEqualTo("EXT123456");
        assertThat(result.getCompletedAt()).isNotNull();
        verify(transactionRepository).save(testTransaction);
    }

    @Test
    void updateTransactionOnPaymentSuccess_TransactionNotFound_ThrowsException() {
        // Given
        when(transactionRepository.findByPayment(testPayment)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.updateTransactionOnPaymentSuccess(testPayment, "EXT123456"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Transaction không tồn tại");
    }

    @Test
    void getUserTransactions_WithType_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> transactionsPage = new PageImpl<>(Arrays.asList(testTransaction));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByUserAndTypeOrderByCreatedAtDesc(testUser, TransactionType.PAYMENT, pageable))
                .thenReturn(transactionsPage);

        // When
        Page<TransactionResponse> result = transactionService.getUserTransactions(1L, TransactionType.PAYMENT, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getType()).isEqualTo(TransactionType.PAYMENT);
        verify(transactionRepository).findByUserAndTypeOrderByCreatedAtDesc(testUser, TransactionType.PAYMENT, pageable);
    }

    @Test
    void getUserTransactions_WithoutType_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> transactionsPage = new PageImpl<>(Arrays.asList(testTransaction));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByUserOrderByCreatedAtDesc(testUser, pageable))
                .thenReturn(transactionsPage);

        // When
        Page<TransactionResponse> result = transactionService.getUserTransactions(1L, null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(transactionRepository).findByUserOrderByCreatedAtDesc(testUser, pageable);
    }

    @Test
    void getUserTransactions_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.getUserTransactions(999L, null, PageRequest.of(0, 10)))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void getAllTransactions_WithFilters_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        Page<Transaction> transactionsPage = new PageImpl<>(Arrays.asList(testTransaction));

        when(transactionRepository.findAllWithFilters(
                eq(TransactionType.PAYMENT),
                eq(TransactionStatus.SUCCESS),
                eq(startDate),
                any(LocalDateTime.class), // endDate + 1 day
                eq(pageable)
        )).thenReturn(transactionsPage);

        // When
        Page<TransactionResponse> result = transactionService.getAllTransactions(
                TransactionType.PAYMENT,
                TransactionStatus.SUCCESS,
                startDate,
                endDate,
                pageable
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getTransactionSummary_AllTypes_Success() {
        // Given
        when(transactionRepository.count()).thenReturn(100L);
        when(transactionRepository.countByStatus(TransactionStatus.PENDING)).thenReturn(10L);
        when(transactionRepository.sumAmountByFilters(TransactionType.REFUND, TransactionStatus.SUCCESS))
                .thenReturn(BigDecimal.valueOf(500.00));
        when(transactionRepository.sumAmountByFilters(TransactionType.DEPOSIT, TransactionStatus.SUCCESS))
                .thenReturn(BigDecimal.valueOf(1000.00));
        when(transactionRepository.sumAmountByFilters(TransactionType.PAYMENT, TransactionStatus.SUCCESS))
                .thenReturn(BigDecimal.valueOf(-800.00));
        when(transactionRepository.sumAmountByFilters(TransactionType.WITHDRAWAL, TransactionStatus.SUCCESS))
                .thenReturn(BigDecimal.valueOf(-200.00));

        // When
        TransactionSummaryResponse result = transactionService.getTransactionSummary(null, null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalTransactions()).isEqualTo(100L);
        assertThat(result.getPendingTransactions()).isEqualTo(10L);
        assertThat(result.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(1500.00)); // 500 + 1000
        assertThat(result.getTotalExpense()).isEqualByComparingTo(BigDecimal.valueOf(1000.00)); // |−800| + |−200|
        assertThat(result.getNetAmount()).isEqualByComparingTo(BigDecimal.valueOf(500.00)); // 1500 - 1000
    }

    @Test
    void getTransactionSummary_OnlyPayments_Success() {
        // Given
        when(transactionRepository.count()).thenReturn(50L);
        when(transactionRepository.countByStatus(TransactionStatus.PENDING)).thenReturn(5L);
        when(transactionRepository.sumAmountByFilters(TransactionType.PAYMENT, TransactionStatus.SUCCESS))
                .thenReturn(BigDecimal.valueOf(-800.00));

        // When
        TransactionSummaryResponse result = transactionService.getTransactionSummary(
                TransactionType.PAYMENT, null, null, null
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalExpense()).isEqualByComparingTo(BigDecimal.valueOf(800.00));
        assertThat(result.getNetAmount()).isEqualByComparingTo(BigDecimal.valueOf(-800.00));
    }

    @Test
    void getTransactionDetail_Success() {
        // Given
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        // When
        TransactionResponse result = transactionService.getTransactionDetail(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTransactionCode()).isEqualTo("TXN123456789");
        assertThat(result.getType()).isEqualTo(TransactionType.PAYMENT);
        assertThat(result.getUserName()).isEqualTo("Test User");
        assertThat(result.getUserEmail()).isEqualTo("test@example.com");
    }

    @Test
    void getTransactionDetail_NotFound_ThrowsException() {
        // Given
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.getTransactionDetail(999L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Transaction không tồn tại");
    }

    @Test
    void generateTransactionCode_ReturnsUniqueCode() {
        // When
        String code1 = transactionService.generateTransactionCode();
        String code2 = transactionService.generateTransactionCode();

        // Then
        assertThat(code1).startsWith("TXN");
        assertThat(code2).startsWith("TXN");
        assertThat(code1).hasSize(15); // TXN + 12 digits
        // Codes should be different (with high probability)
    }
}