package iuh.fit.se.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String ward;
    private String detail;

    private boolean isDefault = false;

    @Column(nullable = true)
    private Double latitude;   // Vĩ độ (optional - có thể null nếu user nhập tay)

    @Column(nullable = true)
    private Double longitude;  // Kinh độ (optional - có thể null nếu user nhập tay)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
