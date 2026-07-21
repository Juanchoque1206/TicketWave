package com.insert7team.TicketWave.notification.service;

import com.insert7team.TicketWave.common.enums.NotificationChannel;
import com.insert7team.TicketWave.common.enums.NotificationType;
import com.insert7team.TicketWave.event.entity.Event;
import com.insert7team.TicketWave.notification.entity.NotificationLog;
import com.insert7team.TicketWave.notification.repository.NotificationLogRepository;
import com.insert7team.TicketWave.order.entity.Order;
import com.insert7team.TicketWave.payment.entity.Refund;
import com.insert7team.TicketWave.ticket.entity.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    public void sendPurchaseConfirmation(Order order) {
        String subject = "Order Confirmed - " + order.getOrderNumber();
        String body = String.format("Your order %s has been confirmed. Total: %s %s",
                order.getOrderNumber(), order.getTotalAmount(), order.getCurrency());
        sendAndLog(order.getUser().getId(), order.getUser().getEmail(), subject, body,
                NotificationType.PURCHASE_CONFIRMATION);
    }

    @Override
    public void sendTicketIssued(Ticket ticket) {
        String subject = "Your Ticket - " + ticket.getEvent().getTitle();
        String body = String.format("Your ticket for %s is ready. Ticket code: %s. Show this at the venue.",
                ticket.getEvent().getTitle(), ticket.getTicketCode());
        sendAndLog(ticket.getUser().getId(), ticket.getUser().getEmail(), subject, body,
                NotificationType.TICKET_ISSUED);
    }

    @Override
    public void sendEventChanged(Event event, String changeDescription) {
        String subject = "Event Update - " + event.getTitle();
        String body = String.format("The event '%s' has been updated: %s", event.getTitle(), changeDescription);
        log.info("Event changed notification for event {}: {}", event.getId(), changeDescription);
        // In a real system, we'd look up all ticket holders and notify each one
    }

    @Override
    public void sendEventCancelled(Event event) {
        String subject = "Event Cancelled - " + event.getTitle();
        String body = String.format("The event '%s' has been cancelled. Refunds will be processed automatically.",
                event.getTitle());
        log.info("Event cancelled notification for event {}", event.getId());
    }

    @Override
    public void sendRefundProcessed(Refund refund) {
        String subject = "Refund Processed";
        String body = String.format("Your refund of %s has been processed. Reason: %s",
                refund.getAmount(), refund.getReason());
        Long userId = refund.getPayment().getOrder().getUser().getId();
        String email = refund.getPayment().getOrder().getUser().getEmail();
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
                    notification.setSent(true);
                    notification.setSentAt(LocalDateTime.now());
                    notification.setErrorMessage(null);
                    notificationLogRepository.save(notification);
                    log.info("Retry successful for notification {}", notification.getId());
                }
            } catch (Exception e) {
                notification.setErrorMessage(e.getMessage());
                notificationLogRepository.save(notification);
                log.warn("Retry failed for notification {}: {}", notification.getId(), e.getMessage());
            }
        }
    }

    private void sendAndLog(Long userId, String email, String subject, String body, NotificationType type) {
        NotificationLog notification = new NotificationLog();
        notification.setUserId(userId);
        notification.setRecipientEmail(email);
        notification.setType(type);
        notification.setChannel(NotificationChannel.EMAIL);
        notification.setSubject(subject);
        notification.setBody(body);

        try {
            boolean success = emailSender.sendEmail(email, subject, body);
            notification.setSent(success);
            if (success) {
                notification.setSentAt(LocalDateTime.now());
            }
        } catch (Exception e) {
            notification.setSent(false);
            notification.setErrorMessage(e.getMessage());
            log.error("Failed to send notification to {}: {}", email, e.getMessage());
        }
        notificationLogRepository.save(notification);
    }
}
