package com.insert7team.TicketWave.notification.service;

public interface EmailSender {
    boolean sendEmail(String to, String subject, String body);
}
