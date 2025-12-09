package iuh.fit.se.ecommerce.dto.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {
    private String name;
    private String brand;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private MultipartFile mainImage;
    private List<MultipartFile> images;
    private List<String> imagesToDelete;
    private String productType;
    private Long promotionId;
    private List<SpecRequest> specifications;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SpecRequest {
        private String specName;
        private String specValue;
    }
}
