package iuh.fit.se.ecommerce.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import iuh.fit.se.ecommerce.entity.NotificationEntity;
import iuh.fit.se.ecommerce.repository.NotificationRepository;
import iuh.fit.se.ecommerce.service.impl.SiteNotificationService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final SiteNotificationService siteNotificationService;

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
        private Boolean read;
    }

    @PostMapping("/broadcast")
    public NotificationDto broadcast(@RequestBody NotificationDto dto) {
        if (dto == null)
            return null;
        NotificationEntity saved = siteNotificationService.saveAndBroadcast(
                dto.getType(),
                dto.getTitle(),
                dto.getMessage(),
                dto.getUrl(),
                dto.getProductId(),
                dto.getTimestamp());
        if (saved != null) {
            dto.setId(String.valueOf(saved.getId()));
            dto.setTimestamp(saved.getTimestamp() == null ? dto.getTimestamp() : saved.getTimestamp().toEpochMilli());
            dto.setRead(false);
        }
        return dto;
    }

    @GetMapping
    public List<NotificationDto> listRecent() {
        List<NotificationEntity> list = notificationRepository.findTop50ByOrderByTimestampDesc();
        return list.stream().map(e -> new NotificationDto(
                String.valueOf(e.getId()),
                e.getType(),
                e.getTitle(),
                e.getMessage(),
                e.getUrl(),
                e.getProductId(),
                e.getTimestamp() == null ? null : e.getTimestamp().toEpochMilli(),
                e.getReadFlag() == null ? false : e.getReadFlag())).collect(Collectors.toList());
    }

    @PutMapping("/{id}/read")
    public NotificationDto markRead(@PathVariable Long id) {
        NotificationEntity e = notificationRepository.findById(id).orElse(null);
        if (e == null)
            return null;
        e.setReadFlag(true);
        notificationRepository.save(e);
        return new NotificationDto(String.valueOf(e.getId()), e.getType(), e.getTitle(), e.getMessage(), e.getUrl(),
                e.getProductId(), e.getTimestamp() == null ? null : e.getTimestamp().toEpochMilli(), e.getReadFlag());
    }

    @PutMapping("/read-all")
    public void markAllRead() {
        List<NotificationEntity> list = notificationRepository.findTop50ByOrderByTimestampDesc();
        list.forEach(n -> n.setReadFlag(true));
        notificationRepository.saveAll(list);
    }
}
