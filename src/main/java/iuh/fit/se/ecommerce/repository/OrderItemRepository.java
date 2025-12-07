package iuh.fit.se.ecommerce.repository;

import iuh.fit.se.ecommerce.entity.OrderItem;
import iuh.fit.se.ecommerce.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
            SELECT COALESCE(SUM(oi.quantity), 0)
            FROM OrderItem oi
            JOIN oi.order o
            WHERE o.status IN :statuses
              AND o.createdAt >= :start
              AND o.createdAt < :end
            """)
    Long sumQuantityByOrderStatusAndCreatedAtBetween(List<OrderStatus> statuses,
                                                     LocalDateTime start,
                                                     LocalDateTime end);
}


