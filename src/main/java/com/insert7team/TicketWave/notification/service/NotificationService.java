package com.insert7team.TicketWave.notification.service;

import com.insert7team.TicketWave.event.entity.Event;
import com.insert7team.TicketWave.order.entity.Order;
import com.insert7team.TicketWave.payment.entity.Refund;
import com.insert7team.TicketWave.ticket.entity.Ticket;

public interface NotificationService {
    void sendPurchaseConfirmation(Order order);
    void sendTicketIssued(Ticket ticket);
    void sendEventChanged(Event event, String changeDescription);
    void sendEventCancelled(Event event);
    void sendRefundProcessed(Refund refund);
    void retryFailedNotifications();
}
