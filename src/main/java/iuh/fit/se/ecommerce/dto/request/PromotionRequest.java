package iuh.fit.se.ecommerce.dto.request;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionRequest {
    private String name;
    private String description;
    private Integer discountPercent;
    private LocalDate startDate;
    private LocalDate endDate;
}
