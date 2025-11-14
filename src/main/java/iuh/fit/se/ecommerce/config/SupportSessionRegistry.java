package iuh.fit.se.ecommerce.config;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
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

    private final Map<String, String> sessionToAdmin = new ConcurrentHashMap<>();
    private final Map<String, PendingRequest> pendingBySession = new ConcurrentHashMap<>();
    private final Set<String> onlineAdmins = ConcurrentHashMap.newKeySet();

    public boolean isAssigned(String sessionId) {
        return sessionToAdmin.containsKey(sessionId);
    }

    public String getAssignedAdmin(String sessionId) {
        return sessionToAdmin.get(sessionId);
    }

    public void assign(String adminId, String sessionId) {
        sessionToAdmin.put(sessionId, adminId);
        pendingBySession.remove(sessionId);
    }

    public void unassign(String sessionId) {
        sessionToAdmin.remove(sessionId);
    }

    public void addPending(String sessionId, String lastQuestion, Long productId) {
        pendingBySession.put(sessionId, new PendingRequest(sessionId, lastQuestion, productId));
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
}
