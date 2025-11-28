package iuh.fit.se.ecommerce.controller;

import iuh.fit.se.ecommerce.entity.NotificationEntity;
import iuh.fit.se.ecommerce.repository.SiteNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/site-notifications")
@RequiredArgsConstructor
public class SiteNotificationController {

    private final SiteNotificationRepository repository;

    @GetMapping
    public ResponseEntity<List<NotificationEntity>> getAllSiteNotifications() {
        List<NotificationEntity> all = repository.findAll();
        List<NotificationEntity> sorted = all.stream()
                .sorted(Comparator.comparing(NotificationEntity::getTimestamp).reversed())
                .collect(Collectors.toList());
        return ResponseEntity.ok(sorted);
    }
}

