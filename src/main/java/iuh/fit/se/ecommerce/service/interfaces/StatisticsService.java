package iuh.fit.se.ecommerce.service.interfaces;

import iuh.fit.se.ecommerce.dto.response.StatisticsResponse;
import iuh.fit.se.ecommerce.dto.response.StatisticsDetailResponse;

import java.time.LocalDateTime;

public interface StatisticsService {
    StatisticsResponse getStatistics(String period);
    StatisticsResponse getStatisticsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    // Methods mới cho dữ liệu chi tiết
    StatisticsDetailResponse getStatisticsDetail(String period);
    StatisticsDetailResponse getStatisticsDetailByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}


