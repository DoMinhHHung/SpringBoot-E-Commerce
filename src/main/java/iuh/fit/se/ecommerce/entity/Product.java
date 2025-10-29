package iuh.fit.se.ecommerce.entity;

import iuh.fit.se.ecommerce.entity.enums.ProductType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String brand;

    @Column(length = 2000)
    private String description;

    private BigDecimal price;
    private Integer stock;

    private String mainImage;

    @ElementCollection
    private List<String> images;

    @Enumerated(EnumType.STRING)
    private ProductType productType;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Specification> specifications;

    @ManyToOne
    private Promotion promotion;
}