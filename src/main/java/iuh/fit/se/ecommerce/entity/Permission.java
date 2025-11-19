package iuh.fit.se.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String code;  // VD: PRODUCT_CREATE, ORDER_UPDATE

    @Column(nullable = false, length = 100)
    private String name;

    private String description;

    @Column(nullable = false, length = 50)
    private String resource;  // VD: PRODUCT, ORDER, TRANSACTION

    @Column(nullable = false, length = 50)
    private String action;   // VD: CREATE, UPDATE, DELETE, VIEW

    @CreationTimestamp
    private LocalDateTime createdAt;
}

