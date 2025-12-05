package iuh.fit.se.ecommerce.repository;

import iuh.fit.se.ecommerce.entity.Order;
import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(Long orderCode);
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    List<Order> findByUserAndStatusOrderByCreatedAtDesc(User user, iuh.fit.se.ecommerce.entity.enums.OrderStatus status);

    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0)
            FROM Order o
            WHERE o.status IN :statuses
              AND o.createdAt >= :start
              AND o.createdAt < :end
            """)
    BigDecimal sumTotalAmountByStatusAndCreatedAtBetween(List<OrderStatus> statuses,
                                                         LocalDateTime start,
                                                         LocalDateTime end);

    @Query("""
            SELECT COUNT(DISTINCT o.user.id)
            FROM Order o
            WHERE o.status IN :statuses
              AND o.createdAt >= :start
              AND o.createdAt < :end
            """)
    Long countDistinctUserByStatusAndCreatedAtBetween(List<OrderStatus> statuses,
                                                      LocalDateTime start,
                                                      LocalDateTime end);
}

