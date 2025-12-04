package iuh.fit.se.ecommerce.dto;

import java.time.LocalDateTime;

public class NotificationDTO {
    private Long id;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
    private String type;
    private Long refId;

    public NotificationDTO() {}

    public NotificationDTO(Long id, String title, String message, boolean read, LocalDateTime createdAt, String type, Long refId) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
        this.type = type;
        this.refId = refId;
    }

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getRefId() { return refId; }
    public void setRefId(Long refId) { this.refId = refId; }
}

