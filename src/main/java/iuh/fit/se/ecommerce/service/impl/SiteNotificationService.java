package iuh.fit.se.ecommerce.service.impl;

import iuh.fit.se.ecommerce.entity.NotificationEntity;
import iuh.fit.se.ecommerce.repository.SiteNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SiteNotificationService {

    private final SiteNotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationEntity saveAndBroadcast(String type,
            String title,
            String message,
            String url,
            Long productId,
            Long timestampMillis) {
        NotificationEntity entity = NotificationEntity.builder()
                .type(type)
                .title(title)
                .message(message)
                .url(url)
                .productId(productId)
                .timestamp(timestampMillis != null ? Instant.ofEpochMilli(timestampMillis) : Instant.now())
                .readFlag(false)
                .build();
        // use raw CrudRepository to avoid generic inference issues from conflicting Notification repo types
        NotificationEntity saved = (NotificationEntity) ((org.springframework.data.repository.CrudRepository) notificationRepository).save(entity);
        log.info("SiteNotification saved id={} type={} title={}", saved.getId(), saved.getType(), saved.getTitle());
        broadcastEntity(saved);
        return saved;
    }

    public void broadcastEntity(NotificationEntity saved) {
        if (saved == null)
            return;
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", saved.getId());
            payload.put("type", saved.getType());
            payload.put("title", saved.getTitle());
            payload.put("message", saved.getMessage());
            payload.put("url", saved.getUrl());
            payload.put("productId", saved.getProductId());
            payload.put("timestamp",
                    saved.getTimestamp() == null ? System.currentTimeMillis() : saved.getTimestamp().toEpochMilli());
            payload.put("read", saved.getReadFlag());
            log.info("Broadcasting site notification id={} topic=/topic/site.notifications payload={}", saved.getId(), payload);
            messagingTemplate.convertAndSend("/topic/site.notifications", payload);
        } catch (Exception ex) {
            log.error("Error broadcasting site notification: {}", ex.getMessage(), ex);
        }
    }
}
