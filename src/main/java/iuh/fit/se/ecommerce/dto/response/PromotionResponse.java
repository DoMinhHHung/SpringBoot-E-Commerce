package iuh.fit.se.ecommerce.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionResponse {
    private Long id;
    private String name;
    private String description;
    private Integer discountPercent;
    private LocalDate startDate;
    private LocalDate endDate;
}
