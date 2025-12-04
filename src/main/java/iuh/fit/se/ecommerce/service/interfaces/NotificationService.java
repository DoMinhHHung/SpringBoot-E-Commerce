package iuh.fit.se.ecommerce.service.interfaces;

import iuh.fit.se.ecommerce.dto.NotificationDTO;
import iuh.fit.se.ecommerce.entity.Notification;
import iuh.fit.se.ecommerce.repository.NotificationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

public interface NotificationService {
    Notification createAndPush(Long userId, String title, String message, String type, Long refId);
    List<NotificationDTO> getNotifications(Long userId);
    long countUnread(Long userId);
    void markRead(Long id);
}
