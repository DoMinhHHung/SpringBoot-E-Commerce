package iuh.fit.se.ecommerce.entity;

import iuh.fit.se.ecommerce.entity.enums.PaymentMethod;
import iuh.fit.se.ecommerce.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentMethod method = PaymentMethod.PAYOS;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    private String paymentLinkId;      // PayOS payment link ID
    private String qrCode;              // QR code image (base64 hoặc URL)
    private String checkoutUrl;         // PayOS checkout URL

    private BigDecimal amount;          // Số tiền thanh toán

    private String transactionId;       // Mã giao dịch từ PayOS
    private LocalDateTime paidAt;       // Thời gian thanh toán

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

