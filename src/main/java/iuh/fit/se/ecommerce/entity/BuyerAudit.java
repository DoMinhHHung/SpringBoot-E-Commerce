package iuh.fit.se.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "buyer_audit", indexes = {
    @Index(name = "idx_recorded_at", columnList = "recorded_at"),
    @Index(name = "idx_user_order", columnList = "user_id,order_code")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_order", columnNames = {"user_id", "order_code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyerAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long orderCode;

    @Column(nullable = false)
    private LocalDateTime firstPurchaseAt; // Lần đầu mua trong khoảng thời gian

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

