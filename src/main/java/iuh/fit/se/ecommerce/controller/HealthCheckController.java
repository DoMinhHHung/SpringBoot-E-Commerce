package iuh.fit.se.ecommerce.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

/**
 * Controller đơn giản để kiểm tra tình trạng (health) của ứng dụng.
 */
@RestController
public class HealthCheckController {

    @GetMapping("/api/health")
    public Map<String, String> checkHealth() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Service is running");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));

        return response;
    }
}