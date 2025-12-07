package iuh.fit.se.ecommerce.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsDetailResponse {
    private String period;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String granularity; // "HOUR", "DAY", "MONTH"
    private List<StatisticsDataPoint> dataPoints; // Danh sách các điểm dữ liệu
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatisticsDataPoint {
        private String label; // "00:00", "01:00", "2024-01-01", "Tháng 1", etc.
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private BigDecimal revenue;
        private Long buyerCount;
        private Long newCustomers;
        private Long productsSold;
    }
}

