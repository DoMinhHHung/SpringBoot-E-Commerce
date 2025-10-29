package iuh.fit.se.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Specification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String specName;
    private String specValue;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
