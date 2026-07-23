package com.insert7team.TicketWave.notification.service;

import com.insert7team.TicketWave.notification.domain.NotificationChannel;
import com.insert7team.TicketWave.notification.domain.NotificationType;
import com.insert7team.TicketWave.notification.entity.NotificationLog;
import com.insert7team.TicketWave.notification.repository.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationLogRepository notificationLogRepository;
    private final EmailSender emailSender;

    public NotificationServiceImpl(NotificationLogRepository notificationLogRepository, EmailSender emailSender) {
        this.notificationLogRepository = notificationLogRepository;
        this.emailSender = emailSender;
    }

    @Override
    public void sendPurchaseConfirmation(Long userId, String email, String orderNumber,
                                          BigDecimal totalAmount, String currency) {
        String subject = "Order Confirmed - " + orderNumber;
        String body = String.format("Your order %s has been confirmed. Total: %s %s",
                orderNumber, totalAmount, currency);
        sendAndLog(userId, email, subject, body, NotificationType.PURCHASE_CONFIRMATION);
    }

    @Override
    public void sendTicketIssued(Long userId, String email, String eventTitle, String ticketCode) {
        String subject = "Your Ticket - " + eventTitle;
        String body = String.format("Your ticket for %s is ready. Ticket code: %s. Show this at the venue.",
                eventTitle, ticketCode);
        sendAndLog(userId, email, subject, body, NotificationType.TICKET_ISSUED);
    }

    @Override
    public void sendEventChanged(Long eventId, String eventTitle, String changeDescription) {
        String subject = "Event Update - " + eventTitle;
        String body = String.format("The event '%s' has been updated: %s", eventTitle, changeDescription);
        log.info("Event changed notification for event {}: {}", eventId, changeDescription);
    }

    @Override
    public void sendEventCancelled(Long eventId, String eventTitle) {
        String subject = "Event Cancelled - " + eventTitle;
        String body = String.format("The event '%s' has been cancelled. Refunds will be processed automatically.",
                eventTitle);
        log.info("Event cancelled notification for event {}", eventId);
    }

    @Override
    public void sendRefundProcessed(Long userId, String email, BigDecimal amount, String reason) {
        String subject = "Refund Processed";
        String body = String.format("Your refund of %s has been processed. Reason: %s", amount, reason);
        sendAndLog(userId, email, subject, body, NotificationType.REFUND_PROCESSED);
    }

    @Override
    public void retryFailedNotifications() {
        List<NotificationLog> failed = notificationLogRepository.findBySentFalse();
        for (NotificationLog notification : failed) {
            try {
                boolean success = emailSender.sendEmail(notification.getRecipientEmail(),
                        notification.getSubject(), notification.getBody());
                if (success) {
                    notification.markSent();
                    notificationLogRepository.save(notification);
                    log.info("Retry successful for notification {}", notification.getId());
                }
            } catch (Exception e) {
                notification.markFailed(e.getMessage());
                notificationLogRepository.save(notification);
                log.warn("Retry failed for notification {}: {}", notification.getId(), e.getMessage());
            }
        }
    }

    private void sendAndLog(Long userId, String email, String subject, String body, NotificationType type) {
        NotificationLog notification = NotificationLog.create(userId, email, type,
                NotificationChannel.EMAIL, subject, body);

        try {
            boolean success = emailSender.sendEmail(email, subject, body);
            if (success) {
                notification.markSent();
            }
        } catch (Exception e) {
            notification.markFailed(e.getMessage());
            log.error("Failed to send notification to {}: {}", email, e.getMessage());
        }
        notificationLogRepository.save(notification);
    }
}
