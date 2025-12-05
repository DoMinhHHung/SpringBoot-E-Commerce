package iuh.fit.se.ecommerce.controller;

import iuh.fit.se.ecommerce.dto.response.StatisticsResponse;
import iuh.fit.se.ecommerce.service.interfaces.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> getStatistics(
            @RequestParam(name = "period", defaultValue = "day") String period) {
        return ResponseEntity.ok(statisticsService.getStatistics(period));
    }
}


