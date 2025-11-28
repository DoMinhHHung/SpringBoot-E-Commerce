package iuh.fit.se.ecommerce.service.impl;

import iuh.fit.se.ecommerce.dto.NotificationDTO;
import iuh.fit.se.ecommerce.entity.Notification;
import iuh.fit.se.ecommerce.repository.NotificationRepository;
import iuh.fit.se.ecommerce.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository repo;
    private final SimpMessagingTemplate messaging;


    public Notification createAndPush(Long userId, String title, String message, String type, Long refId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        n.setRefId(refId);
        n = repo.save(n);

        NotificationDTO dto = new NotificationDTO(n.getId(), n.getTitle(), n.getMessage(), n.isReadFlag(), n.getCreatedAt(), n.getType(), n.getRefId());
        messaging.convertAndSend("/topic/notifications/" + userId, dto);
        return n;
    }

    public List<NotificationDTO> getNotifications(Long userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(n -> new NotificationDTO(n.getId(), n.getTitle(), n.getMessage(), n.isReadFlag(), n.getCreatedAt(), n.getType(), n.getRefId()))
                .collect(Collectors.toList());
    }

    public long countUnread(Long userId) {
        return repo.countByUserIdAndReadFlagFalse(userId);
    }

    public void markRead(Long id) {
        repo.findById(id).ifPresent(n -> {
            n.setReadFlag(true);
            repo.save(n);
        });
    }
}