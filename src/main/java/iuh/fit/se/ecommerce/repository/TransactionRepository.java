package iuh.fit.se.ecommerce.repository;

import iuh.fit.se.ecommerce.entity.Order;
import iuh.fit.se.ecommerce.entity.Payment;
import iuh.fit.se.ecommerce.entity.Transaction;
import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.entity.enums.TransactionStatus;
import iuh.fit.se.ecommerce.entity.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Lấy tất cả transaction của user
    Page<Transaction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // Lấy transaction theo user và type
    Page<Transaction> findByUserAndTypeOrderByCreatedAtDesc(User user, TransactionType type, Pageable pageable);

    // Lấy transaction theo order
    List<Transaction> findByOrder(Order order);

    // Lấy transaction theo payment
    Optional<Transaction> findByPayment(Payment payment);

    // Lấy transaction theo transaction code
    Optional<Transaction> findByTransactionCode(String transactionCode);

    // Lấy transaction theo external transaction ID
    Optional<Transaction> findByExternalTransactionId(String externalTransactionId);

    // Lấy transaction trong khoảng thời gian
    Page<Transaction> findByUserAndCreatedAtBetween(
            User user,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    // Lấy tất cả transactions với filters (cho admin)
      @Query(value = """
            SELECT * FROM transactions t WHERE 
            (:type IS NULL OR t.type = CAST(:type AS VARCHAR)) AND 
            (:status IS NULL OR t.status = CAST(:status AS VARCHAR)) AND 
            (:startDate IS NULL OR t.created_at >= CAST(:startDate AS TIMESTAMP)) AND 
            (:endDate IS NULL OR t.created_at <= CAST(:endDate AS TIMESTAMP))
            ORDER BY t.created_at DESC
            """, nativeQuery = true)
    Page<Transaction> findAllWithFilters(
            @Param("type") TransactionType type,
            @Param("status") TransactionStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // Tính tổng số tiền theo type và status
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE " +
            "(:type IS NULL OR t.type = :type) AND " +
            "(:status IS NULL OR t.status = :status)")
    BigDecimal sumAmountByFilters(
            @Param("type") TransactionType type,
            @Param("status") TransactionStatus status
    );

    // Đếm số transaction theo status
    @Query("SELECT COUNT(t) FROM Transaction t WHERE " +
            "(:status IS NULL OR t.status = :status)")
    Long countByStatus(@Param("status") TransactionStatus status);
}

