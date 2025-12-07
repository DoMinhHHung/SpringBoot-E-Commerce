package iuh.fit.se.ecommerce.repository;

import iuh.fit.se.ecommerce.entity.BuyerAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BuyerAuditRepository extends JpaRepository<BuyerAudit, Long> {
    
    @Query("""
            SELECT COUNT(DISTINCT b.userId)
            FROM BuyerAudit b
            WHERE b.recordedAt >= :start
              AND b.recordedAt < :end
            """)
    Long countDistinctBuyersByRecordedAtBetween(@Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end);
    
    boolean existsByUserIdAndOrderCode(Long userId, Long orderCode);
    
    // Query theo giờ
    @Query(value = """
            SELECT DATE_FORMAT(recorded_at, '%Y-%m-%d %H:00:00') as timeLabel,
                   MIN(recorded_at) as startTime,
                   MAX(recorded_at) as endTime,
                   COUNT(DISTINCT user_id) as buyerCount
            FROM buyer_audit
            WHERE recorded_at >= :start
              AND recorded_at < :end
            GROUP BY DATE_FORMAT(recorded_at, '%Y-%m-%d %H:00:00')
            ORDER BY timeLabel
            """, nativeQuery = true)
    List<Object[]> countBuyersByHour(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Query theo ngày
    @Query(value = """
            SELECT DATE_FORMAT(recorded_at, '%Y-%m-%d') as timeLabel,
                   MIN(recorded_at) as startTime,
                   MAX(recorded_at) as endTime,
                   COUNT(DISTINCT user_id) as buyerCount
            FROM buyer_audit
            WHERE recorded_at >= :start
              AND recorded_at < :end
            GROUP BY DATE_FORMAT(recorded_at, '%Y-%m-%d')
            ORDER BY timeLabel
            """, nativeQuery = true)
    List<Object[]> countBuyersByDay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Query theo tháng
    @Query(value = """
            SELECT DATE_FORMAT(recorded_at, '%Y-%m') as timeLabel,
                   MIN(recorded_at) as startTime,
                   MAX(recorded_at) as endTime,
                   COUNT(DISTINCT user_id) as buyerCount
            FROM buyer_audit
            WHERE recorded_at >= :start
              AND recorded_at < :end
            GROUP BY DATE_FORMAT(recorded_at, '%Y-%m')
            ORDER BY timeLabel
            """, nativeQuery = true)
    List<Object[]> countBuyersByMonth(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

