package iuh.fit.se.ecommerce.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailResponse {
    private Long id;
    private String name;
    private String brand;
    private Integer stock;
    private BigDecimal price;
    private BigDecimal priceAfterDiscount;
    private String description;
    private String mainImage;
    private List<String> images;
    private String productType;
    private List<Map<String, String>> specifications;
    private Long promotionId;
}