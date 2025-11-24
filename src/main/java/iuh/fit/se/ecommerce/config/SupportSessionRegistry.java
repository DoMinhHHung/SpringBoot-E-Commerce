package iuh.fit.se.ecommerce.config;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import iuh.fit.se.ecommerce.entity.SupportSession;
import iuh.fit.se.ecommerce.repository.SupportSessionRepository;

@Component
@RequiredArgsConstructor
public class SupportSessionRegistry {

    public static class PendingRequest {
        public final String sessionId;
        public final String lastQuestion;
        public final Long productId;
        public final Instant createdAt;

        public PendingRequest(String sessionId, String lastQuestion, Long productId) {
            this.sessionId = sessionId;
            this.lastQuestion = lastQuestion;
            this.productId = productId;
            this.createdAt = Instant.now();
        }
    }

    private final SimpMessagingTemplate messagingTemplate;
    private final SupportSessionRepository sessionRepository;
    private final Map<String, String> sessionToAdmin = new ConcurrentHashMap<>();
    private final Map<String, PendingRequest> pendingBySession = new ConcurrentHashMap<>();
    private final Set<String> onlineAdmins = ConcurrentHashMap.newKeySet();

    public boolean isAssigned(String sessionId) {
        return sessionToAdmin.containsKey(sessionId) || sessionRepository.findById(sessionId).map(s -> "ASSIGNED".equals(s.getStatus())).orElse(false);
    }

    public String getAssignedAdmin(String sessionId) {
        String inMemory = sessionToAdmin.get(sessionId);
        if (inMemory != null) return inMemory;
        return sessionRepository.findById(sessionId).map(SupportSession::getAssignedAdmin).orElse(null);
    }

    public void assign(String adminId, String sessionId) {
        sessionToAdmin.put(sessionId, adminId);
        pendingBySession.remove(sessionId);
        // persist
        try {
            SupportSession s = sessionRepository.findById(sessionId).orElse(SupportSession.of(sessionId, null, null));
            s.setAssignedAdmin(adminId);
            s.setStatus("ASSIGNED");
            sessionRepository.save(s);
        } catch (Exception ex) {
            System.err.println("Failed to persist assigned support session: " + ex.getMessage());
        }
        notifyPendingUpdated();
    }

    public void unassign(String sessionId) {
        sessionToAdmin.remove(sessionId);
        try {
            sessionRepository.findById(sessionId).ifPresent(s -> {
                s.setAssignedAdmin(null);
                s.setStatus("CLOSED");
                sessionRepository.save(s);
            });
        } catch (Exception ex) {
            System.err.println("Failed to persist unassign support session: " + ex.getMessage());
        }
    }

    public void addPending(String sessionId, String lastQuestion, Long productId) {
        pendingBySession.put(sessionId, new PendingRequest(sessionId, lastQuestion, productId));
        // persist
        try {
            SupportSession s = sessionRepository.findById(sessionId).orElse(SupportSession.of(sessionId, lastQuestion, productId));
            s.setLastQuestion(lastQuestion);
            s.setProductId(productId);
            s.setStatus("PENDING");
            sessionRepository.save(s);
        } catch (Exception ex) {
            System.err.println("Failed to persist pending support session: " + ex.getMessage());
        }
        notifyPendingUpdated();
    }

    public Map<String, PendingRequest> getAllPending() {
        return pendingBySession;
    }

    public void adminOnline(String adminId) {
        onlineAdmins.add(adminId);
    }

    public void adminOffline(String adminId) {
        onlineAdmins.remove(adminId);
    }

    public boolean hasOnlineAdmin() {
        return !onlineAdmins.isEmpty();
    }

    public void registerPending(String sessionId, String lastQuestion, Long productId) {
        addPending(sessionId, lastQuestion, productId);
    }

    // Optionally notify when cleared
    public void clearPending(String sessionId) {
        pendingBySession.remove(sessionId);
        try {
            messagingTemplate.convertAndSend("/topic/admin.incoming.clear", sessionId);
        } catch (Exception ex) {
            System.err.println("Failed to notify admins about cleared support: " + ex.getMessage());
        }
        notifyPendingUpdated();
    }

    public SupportSession getSession(String sessionId) {
        return sessionRepository.findById(sessionId).orElse(null);
    }

    private void notifyPendingUpdated() {
        try {
            messagingTemplate.convertAndSend("/topic/admin.incoming", getAllPending().values());
        } catch (Exception ex) {
            System.err.println("Failed to notify admins about pending support: " + ex.getMessage());
        }
    }
}
