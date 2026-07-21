package com.insert7team.TicketWave.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MockEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(MockEmailSender.class);

    @Override
    public boolean sendEmail(String to, String subject, String body) {
        log.info("===== MOCK EMAIL =====");
        log.info("To: {}", to);
        log.info("Subject: {}", subject);
        log.info("Body: {}", body);
        log.info("======================");
        return true;
    }
}
