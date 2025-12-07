package iuh.fit.se.ecommerce.repository;

import iuh.fit.se.ecommerce.entity.ProductSaleAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductSaleAuditRepository extends JpaRepository<ProductSaleAudit, Long> {
    
    @Query("""
            SELECT COALESCE(SUM(p.quantity), 0)
            FROM ProductSaleAudit p
            WHERE p.recordedAt >= :start
              AND p.recordedAt < :end
            """)
    Long sumQuantityByRecordedAtBetween(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);
    
    // Query theo giờ
    @Query(value = """
            SELECT DATE_FORMAT(recorded_at, '%Y-%m-%d %H:00:00') as timeLabel,
                   MIN(recorded_at) as startTime,
                   MAX(recorded_at) as endTime,
                   COALESCE(SUM(quantity), 0) as productsSold
            FROM product_sale_audit
            WHERE recorded_at >= :start
              AND recorded_at < :end
            GROUP BY DATE_FORMAT(recorded_at, '%Y-%m-%d %H:00:00')
            ORDER BY timeLabel
            """, nativeQuery = true)
    List<Object[]> sumProductsSoldByHour(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Query theo ngày
    @Query(value = """
            SELECT DATE_FORMAT(recorded_at, '%Y-%m-%d') as timeLabel,
                   MIN(recorded_at) as startTime,
                   MAX(recorded_at) as endTime,
                   COALESCE(SUM(quantity), 0) as productsSold
            FROM product_sale_audit
            WHERE recorded_at >= :start
              AND recorded_at < :end
            GROUP BY DATE_FORMAT(recorded_at, '%Y-%m-%d')
            ORDER BY timeLabel
            """, nativeQuery = true)
    List<Object[]> sumProductsSoldByDay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Query theo tháng
    @Query(value = """
            SELECT DATE_FORMAT(recorded_at, '%Y-%m') as timeLabel,
                   MIN(recorded_at) as startTime,
                   MAX(recorded_at) as endTime,
                   COALESCE(SUM(quantity), 0) as productsSold
            FROM product_sale_audit
            WHERE recorded_at >= :start
              AND recorded_at < :end
            GROUP BY DATE_FORMAT(recorded_at, '%Y-%m')
            ORDER BY timeLabel
            """, nativeQuery = true)
    List<Object[]> sumProductsSoldByMonth(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

