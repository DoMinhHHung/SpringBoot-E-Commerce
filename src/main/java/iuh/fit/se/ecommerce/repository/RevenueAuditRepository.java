package iuh.fit.se.ecommerce.repository;

import iuh.fit.se.ecommerce.entity.RevenueAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RevenueAuditRepository extends JpaRepository<RevenueAudit, Long> {
    
    @Query("""
            SELECT COALESCE(SUM(r.amount), 0)
            FROM RevenueAudit r
            WHERE r.status = 'CONFIRMED'
              AND r.recordedAt >= :start
              AND r.recordedAt < :end
            """)
    BigDecimal sumRevenueByRecordedAtBetween(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);
    
    // Query theo giờ
    @Query(value = """
            SELECT DATE_FORMAT(recorded_at, '%Y-%m-%d %H:00:00') as timeLabel,
                   MIN(recorded_at) as startTime,
                   MAX(recorded_at) as endTime,
                   COALESCE(SUM(amount), 0) as revenue
            FROM revenue_audit
            WHERE status = 'CONFIRMED'
              AND recorded_at >= :start
              AND recorded_at < :end
            GROUP BY DATE_FORMAT(recorded_at, '%Y-%m-%d %H:00:00')
            ORDER BY timeLabel
            """, nativeQuery = true)
    List<Object[]> sumRevenueByHour(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Query theo ngày
    @Query(value = """
            SELECT DATE_FORMAT(recorded_at, '%Y-%m-%d') as timeLabel,
                   MIN(recorded_at) as startTime,
                   MAX(recorded_at) as endTime,
                   COALESCE(SUM(amount), 0) as revenue
            FROM revenue_audit
            WHERE status = 'CONFIRMED'
              AND recorded_at >= :start
              AND recorded_at < :end
            GROUP BY DATE_FORMAT(recorded_at, '%Y-%m-%d')
            ORDER BY timeLabel
            """, nativeQuery = true)
    List<Object[]> sumRevenueByDay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Query theo tháng
    @Query(value = """
            SELECT DATE_FORMAT(recorded_at, '%Y-%m') as timeLabel,
                   MIN(recorded_at) as startTime,
                   MAX(recorded_at) as endTime,
                   COALESCE(SUM(amount), 0) as revenue
            FROM revenue_audit
            WHERE status = 'CONFIRMED'
              AND recorded_at >= :start
              AND recorded_at < :end
            GROUP BY DATE_FORMAT(recorded_at, '%Y-%m')
            ORDER BY timeLabel
            """, nativeQuery = true)
    List<Object[]> sumRevenueByMonth(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

