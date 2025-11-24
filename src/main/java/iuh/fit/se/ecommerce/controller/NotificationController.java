package iuh.fit.se.ecommerce.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationDto {
        private String id;
        private String type; // product|promotion|info
        private String title;
        private String message;
        private String url;
        private Long productId;
        private Long timestamp;
    }

    @PostMapping("/broadcast")
    public NotificationDto broadcast(@RequestBody NotificationDto dto) {
        if (dto == null) return null;
        if (dto.getTimestamp() == null) dto.setTimestamp(System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/site.notifications", dto);
        return dto;
    }
}
