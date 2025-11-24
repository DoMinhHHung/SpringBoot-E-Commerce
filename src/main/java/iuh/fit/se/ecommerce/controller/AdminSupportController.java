package iuh.fit.se.ecommerce.controller;

import iuh.fit.se.ecommerce.config.SupportSessionRegistry;
import iuh.fit.se.ecommerce.dto.response.ChatResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;

@Controller
@RequiredArgsConstructor
public class AdminSupportController {

    private final SimpMessagingTemplate messagingTemplate;
    private final SupportSessionRegistry registry;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminJoinRequest {
        private String adminId;
        private String sessionId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminChatMessage {
        private String sessionId;
        private String adminId;
        private String text;
        private Long timestamp;
    }

    @MessageMapping("/support/join")
    public void joinSession(@Payload AdminJoinRequest req) {
        if (req == null || req.getSessionId() == null)
            return;
        String adminId = req.getAdminId() == null || req.getAdminId().isBlank() ? "admin" : req.getAdminId();
        registry.assign(adminId, req.getSessionId());

        String notifyUser = "Tư vấn viên đã tham gia cuộc trò chuyện. Bạn có thể đặt câu hỏi trực tiếp.";
        messagingTemplate.convertAndSend("/topic/replies." + req.getSessionId(),
                new ChatResponse(notifyUser, java.util.List.of(), java.util.List.of(
                        new ChatResponse.Suggestion("Rời cuộc trò chuyện", "LEAVE_AGENT"))));

        messagingTemplate.convertAndSend("/topic/admin.session." + req.getSessionId(),
                new AdminChatMessage(req.getSessionId(), adminId, "Đã tham gia phiên.", Instant.now().toEpochMilli()));
    }

    @MessageMapping("/support/adminSend")
    public void adminSend(@Payload AdminChatMessage msg) {
        if (msg == null || msg.getSessionId() == null)
            return;
        long ts = msg.getTimestamp() == null ? Instant.now().toEpochMilli() : msg.getTimestamp();
        AdminChatMessage payload = new AdminChatMessage(msg.getSessionId(),
                msg.getAdminId() == null ? "admin" : msg.getAdminId(), msg.getText(), ts);

        messagingTemplate.convertAndSend("/topic/replies." + msg.getSessionId(),
                new ChatResponse("Tư vấn viên: " + (msg.getText() == null ? "" : msg.getText()), java.util.List.of(),
                        java.util.List.of(
                                new ChatResponse.Suggestion("Rời cuộc trò chuyện", "LEAVE_AGENT"))));
        messagingTemplate.convertAndSend("/topic/admin.session." + msg.getSessionId(), payload);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CloseRequest {
        private String adminId;
        private String sessionId;
    }

    @MessageMapping("/support/close")
    public void closeSession(@Payload CloseRequest req) {
        if (req == null || req.getSessionId() == null)
            return;
        registry.unassign(req.getSessionId());
        messagingTemplate.convertAndSend("/topic/replies." + req.getSessionId(),
                new ChatResponse("Cuộc trò chuyện đã được kết thúc bởi tư vấn viên.", java.util.List.of(),
                        java.util.List.of()));
        messagingTemplate.convertAndSend("/topic/admin.session." + req.getSessionId(),
                new AdminChatMessage(req.getSessionId(), req.getAdminId(), "Đã kết thúc phiên.",
                        Instant.now().toEpochMilli()));
    }
}

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
class AdminSupportRestController {
    private final SupportSessionRegistry registry;

    @GetMapping("/pending")
    public ResponseEntity<?> listPending() {
        return ResponseEntity.ok(registry.getAllPending().values());
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<?> getSession(@PathVariable String sessionId) {
        var s = registry.getSession(sessionId);
        if (s == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(s);
    }
}
