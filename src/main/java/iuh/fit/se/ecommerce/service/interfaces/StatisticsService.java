package iuh.fit.se.ecommerce.service.interfaces;

import iuh.fit.se.ecommerce.dto.response.StatisticsResponse;

public interface StatisticsService {
    StatisticsResponse getStatistics(String period);
}


