package com.insert7team.TicketWave.notification.entity;

import com.insert7team.TicketWave.shared.infrastructure.persistence.BaseEntity;
import com.insert7team.TicketWave.notification.domain.NotificationChannel;
import com.insert7team.TicketWave.notification.domain.NotificationType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs")
public class NotificationLog extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Column(length = 300)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false)
    private boolean sent = false;

    private LocalDateTime sentAt;

    @Column(length = 500)
    private String errorMessage;

    // --- Domain behavior ---

    public static NotificationLog create(Long userId, String recipientEmail,
                                          NotificationType type, NotificationChannel channel,
                                          String subject, String body) {
        NotificationLog log = new NotificationLog();
        log.userId = userId;
        log.recipientEmail = recipientEmail;
        log.type = type;
        log.channel = channel;
        log.subject = subject;
        log.body = body;
        return log;
    }

    public void markSent() {
        this.sent = true;
        this.sentAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    public void markFailed(String error) {
        this.sent = false;
        this.errorMessage = error;
    }

    // --- Getters ---

    public Long getUserId() { return userId; }
    public String getRecipientEmail() { return recipientEmail; }
    public NotificationType getType() { return type; }
    public NotificationChannel getChannel() { return channel; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public boolean isSent() { return sent; }
    public LocalDateTime getSentAt() { return sentAt; }
    public String getErrorMessage() { return errorMessage; }
}
