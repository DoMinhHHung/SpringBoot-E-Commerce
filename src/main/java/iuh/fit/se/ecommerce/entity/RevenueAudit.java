package iuh.fit.se.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "revenue_audit", indexes = {
    @Index(name = "idx_recorded_at", columnList = "recorded_at"),
    @Index(name = "idx_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderCode;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 50)
    private String status; // CONFIRMED, REFUNDED, etc.

    @Column(nullable = false)
    private LocalDateTime recordedAt; // Thời điểm ghi nhận doanh số

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

