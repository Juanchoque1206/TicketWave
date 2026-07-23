package com.insert7team.TicketWave.notification.service;

import java.math.BigDecimal;

public interface NotificationService {
    void sendPurchaseConfirmation(Long userId, String email, String orderNumber,
                                  BigDecimal totalAmount, String currency);
    void sendTicketIssued(Long userId, String email, String eventTitle, String ticketCode);
    void sendEventChanged(Long eventId, String eventTitle, String changeDescription);
    void sendEventCancelled(Long eventId, String eventTitle);
    void sendRefundProcessed(Long userId, String email, BigDecimal amount, String reason);
    void retryFailedNotifications();
}
