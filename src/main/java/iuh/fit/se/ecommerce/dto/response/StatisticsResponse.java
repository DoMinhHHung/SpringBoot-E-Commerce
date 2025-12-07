package iuh.fit.se.ecommerce.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsResponse {
    private String period;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal revenue;
    private Long buyerCount;
    private Long newCustomers;
    private Long productsSold;
}


