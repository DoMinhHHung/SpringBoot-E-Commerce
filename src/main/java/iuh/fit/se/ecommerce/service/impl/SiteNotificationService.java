package iuh.fit.se.ecommerce.service.impl;

import iuh.fit.se.ecommerce.entity.NotificationEntity;
import iuh.fit.se.ecommerce.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SiteNotificationService {

    private final NotificationRepository notificationRepository;
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
        NotificationEntity saved = notificationRepository.save(entity);
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
            messagingTemplate.convertAndSend("/topic/site.notifications", payload);
        } catch (Exception ignored) {
        }
    }
}
