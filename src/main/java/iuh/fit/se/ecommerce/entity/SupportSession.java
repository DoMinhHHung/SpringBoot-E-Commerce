package iuh.fit.se.ecommerce.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "support_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportSession {
    @Id
    private String sessionId;
    private String lastQuestion;
    private Long productId;
    private String assignedAdmin; // admin id or username
    private String status; // PENDING, ASSIGNED, CLOSED
    private Instant createdAt;

    public static SupportSession of(String sessionId, String lastQuestion, Long productId) {
        return SupportSession.builder()
                .sessionId(sessionId)
                .lastQuestion(lastQuestion)
                .productId(productId)
                .status("PENDING")
                .createdAt(Instant.now())
                .build();
    }
}

