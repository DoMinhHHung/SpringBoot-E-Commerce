package iuh.fit.se.ecommerce.controller;

import iuh.fit.se.ecommerce.dto.response.StatisticsResponse;
import iuh.fit.se.ecommerce.dto.response.StatisticsDetailResponse;
import iuh.fit.se.ecommerce.service.interfaces.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> getStatistics(
            @RequestParam(name = "period", required = false) String period,
            @RequestParam(name = "startDate", required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(name = "endDate", required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        // If custom date range is provided, use it; otherwise use period
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(statisticsService.getStatisticsByDateRange(startDate, endDate));
        } else {
            String periodParam = period != null ? period : "day";
            return ResponseEntity.ok(statisticsService.getStatistics(periodParam));
        }
    }

    @GetMapping("/statistics/detail")
    public ResponseEntity<StatisticsDetailResponse> getStatisticsDetail(
            @RequestParam(name = "period", required = false) String period,
            @RequestParam(name = "startDate", required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(name = "endDate", required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(statisticsService.getStatisticsDetailByDateRange(startDate, endDate));
        } else {
            String periodParam = period != null ? period : "day";
            return ResponseEntity.ok(statisticsService.getStatisticsDetail(periodParam));
        }
    }
}


