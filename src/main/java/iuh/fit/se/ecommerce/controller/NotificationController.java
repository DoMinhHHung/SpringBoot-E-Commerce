package iuh.fit.se.ecommerce.controller;

import iuh.fit.se.ecommerce.dto.NotificationDTO;
import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.repository.UserRepository;
import iuh.fit.se.ecommerce.service.interfaces.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;
    private final UserRepository userRepository;

    public NotificationController(NotificationService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> rootNotifications() {
        return ResponseEntity.badRequest().body(Map.of("error", "Missing userId. Use /api/notifications/{userId} or /api/notifications/me"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyNotifications(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthenticated"));
        }
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
        List<NotificationDTO> list = service.getNotifications(user.getId());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{userId}")
    public List<NotificationDTO> getNotifications(@PathVariable Long userId) {
        return service.getNotifications(userId);
    }

    @GetMapping("/me/unreadCount")
    public ResponseEntity<?> getMyUnreadCount(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthenticated"));
        }
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
        long c = service.countUnread(user.getId());
        return ResponseEntity.ok(Map.of("unreadCount", c));
    }

    @GetMapping("/{userId}/unreadCount")
    public long getUnreadCount(@PathVariable Long userId) {
        return service.countUnread(userId);
    }

    @PostMapping("/markRead/{id}")
    public void markRead(@PathVariable Long id) {
        service.markRead(id);
    }
}
