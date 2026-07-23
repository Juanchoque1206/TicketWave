package com.insert7team.TicketWave.notification.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insert7team.TicketWave.shared.infrastructure.messaging.KafkaTopics;
import com.insert7team.TicketWave.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class NotificationKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationKafkaConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationKafkaConsumer(NotificationService notificationService,
                                    ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_COMPLETED, groupId = "ticketwave-notification")
    public void onOrderCompleted(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            Long userId = node.get("userId").asLong();
            String email = node.get("email").asText();
            String orderNumber = node.get("orderNumber").asText();
            BigDecimal totalAmount = new BigDecimal(node.get("totalAmount").asText());
            String currency = node.get("currency").asText();

            notificationService.sendPurchaseConfirmation(userId, email, orderNumber, totalAmount, currency);
            log.info("Sent purchase confirmation for order {}", orderNumber);
        } catch (Exception e) {
            log.error("Error processing ORDER_COMPLETED notification: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = KafkaTopics.EVENT_CHANGED, groupId = "ticketwave-notification")
    public void onEventChanged(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            Long eventId = node.get("eventEntityId").asLong();
            String title = node.has("title") ? node.get("title").asText() : "Unknown event";
            String description = node.has("description") ? node.get("description").asText() : "Details updated";

            notificationService.sendEventChanged(eventId, title, description);
            log.info("Event changed notification received for event {}: {}", eventId, description);
        } catch (Exception e) {
            log.error("Error processing EVENT_CHANGED notification: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = KafkaTopics.EVENT_CANCELLED, groupId = "ticketwave-notification")
    public void onEventCancelled(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            Long eventId = node.get("eventEntityId").asLong();
            String title = node.has("title") ? node.get("title").asText() : "Unknown event";

            notificationService.sendEventCancelled(eventId, title);
            log.info("Event cancelled notification received for event {}", eventId);
        } catch (Exception e) {
            log.error("Error processing EVENT_CANCELLED notification: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = KafkaTopics.REFUND_PROCESSED, groupId = "ticketwave-notification")
    public void onRefundProcessed(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            Long userId = node.get("userId").asLong();
            String email = node.has("email") ? node.get("email").asText() : "";
            BigDecimal amount = node.has("amount") ? new BigDecimal(node.get("amount").asText()) : BigDecimal.ZERO;
            String reason = node.has("reason") ? node.get("reason").asText() : "Refund processed";

            notificationService.sendRefundProcessed(userId, email, amount, reason);
            log.info("Refund notification processed for user {}", userId);
        } catch (Exception e) {
            log.error("Error processing REFUND_PROCESSED notification: {}", e.getMessage());
        }
    }
}
