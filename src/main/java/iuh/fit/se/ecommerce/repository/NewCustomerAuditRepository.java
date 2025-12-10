package iuh.fit.se.ecommerce.repository;

import iuh.fit.se.ecommerce.entity.NewCustomerAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NewCustomerAuditRepository extends JpaRepository<NewCustomerAudit, Long> {
    
    @Query("""
            SELECT COUNT(n)
            FROM NewCustomerAudit n
            WHERE n.registeredAt >= :start
              AND n.registeredAt < :end
            """)
    Long countByRegisteredAtBetween(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);
    
    boolean existsByUserId(Long userId);
    
    // // Query theo giờ
    // @Query(value = """
    //         SELECT DATE_FORMAT(registered_at, '%Y-%m-%d %H:00:00') as timeLabel,
    //                MIN(registered_at) as startTime,
    //                MAX(registered_at) as endTime,
    //                COUNT(*) as newCustomers
    //         FROM new_customer_audit
    //         WHERE registered_at >= :start
    //           AND registered_at < :end
    //         GROUP BY DATE_FORMAT(registered_at, '%Y-%m-%d %H:00:00')
    //         ORDER BY timeLabel
    //         """, nativeQuery = true)
    // List<Object[]> countNewCustomersByHour(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // // Query theo ngày
    // @Query(value = """
    //         SELECT DATE_FORMAT(registered_at, '%Y-%m-%d') as timeLabel,
    //                MIN(registered_at) as startTime,
    //                MAX(registered_at) as endTime,
    //                COUNT(*) as newCustomers
    //         FROM new_customer_audit
    //         WHERE registered_at >= :start
    //           AND registered_at < :end
    //         GROUP BY DATE_FORMAT(registered_at, '%Y-%m-%d')
    //         ORDER BY timeLabel
    //         """, nativeQuery = true)
    // List<Object[]> countNewCustomersByDay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // // Query theo tháng
    // @Query(value = """
    //         SELECT DATE_FORMAT(registered_at, '%Y-%m') as timeLabel,
    //                MIN(registered_at) as startTime,
    //                MAX(registered_at) as endTime,
    //                COUNT(*) as newCustomers
    //         FROM new_customer_audit
    //         WHERE registered_at >= :start
    //           AND registered_at < :end
    //         GROUP BY DATE_FORMAT(registered_at, '%Y-%m')
    //         ORDER BY timeLabel
    //         """, nativeQuery = true)
    // List<Object[]> countNewCustomersByMonth(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);


    // Query theo giờ - PostgreSQL
    @Query(value = """
            SELECT to_char(date_trunc('hour', registered_at), 'YYYY-MM-DD HH24:00:00') as timeLabel,
                   MIN(registered_at) as startTime,
                   MAX(registered_at) as endTime,
                   COUNT(*) as newCustomers
            FROM new_customer_audit
            WHERE registered_at >= :start
              AND registered_at < :end
            GROUP BY date_trunc('hour', registered_at)
            ORDER BY timeLabel
            """, nativeQuery = true)
    List<Object[]> countNewCustomersByHour(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Query theo ngày - PostgreSQL
    @Query(value = """
            SELECT to_char(date_trunc('day', registered_at), 'YYYY-MM-DD') as timeLabel,
                   MIN(registered_at) as startTime,
                   MAX(registered_at) as endTime,
                   COUNT(*) as newCustomers
            FROM new_customer_audit
            WHERE registered_at >= :start
              AND registered_at < :end
            GROUP BY date_trunc('day', registered_at)
            ORDER BY timeLabel
            """, nativeQuery = true)
    List<Object[]> countNewCustomersByDay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Query theo tháng - PostgreSQL
    @Query(value = """
            SELECT to_char(date_trunc('month', registered_at), 'YYYY-MM') as timeLabel,
                   MIN(registered_at) as startTime,
                   MAX(registered_at) as endTime,
                   COUNT(*) as newCustomers
            FROM new_customer_audit
            WHERE registered_at >= :start
              AND registered_at < :end
            GROUP BY date_trunc('month', registered_at)
            ORDER BY timeLabel
            """, nativeQuery = true)
    List<Object[]> countNewCustomersByMonth(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

