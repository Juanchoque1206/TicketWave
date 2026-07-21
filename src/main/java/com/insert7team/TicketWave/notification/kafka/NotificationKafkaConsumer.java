package com.insert7team.TicketWave.notification.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insert7team.TicketWave.common.kafka.KafkaTopics;
import com.insert7team.TicketWave.notification.service.NotificationService;
import com.insert7team.TicketWave.order.entity.Order;
import com.insert7team.TicketWave.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationKafkaConsumer.class);

    private final NotificationService notificationService;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    public NotificationKafkaConsumer(NotificationService notificationService,
                                    OrderRepository orderRepository,
                                    ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_COMPLETED, groupId = "ticketwave-notification")
    public void onOrderCompleted(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            Long orderId = node.get("orderId").asLong();
            orderRepository.findById(orderId).ifPresent(order -> {
                notificationService.sendPurchaseConfirmation(order);
                log.info("Sent purchase confirmation for order {}", orderId);
            });
        } catch (Exception e) {
            log.error("Error processing ORDER_COMPLETED notification: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = KafkaTopics.EVENT_CHANGED, groupId = "ticketwave-notification")
    public void onEventChanged(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String description = node.has("description") ? node.get("description").asText() : "Details updated";
            log.info("Event changed notification received: {}", description);
        } catch (Exception e) {
            log.error("Error processing EVENT_CHANGED notification: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = KafkaTopics.EVENT_CANCELLED, groupId = "ticketwave-notification")
    public void onEventCancelled(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String title = node.has("title") ? node.get("title").asText() : "Unknown event";
            log.info("Event cancelled notification received for: {}", title);
        } catch (Exception e) {
            log.error("Error processing EVENT_CANCELLED notification: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = KafkaTopics.REFUND_PROCESSED, groupId = "ticketwave-notification")
    public void onRefundProcessed(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            Long orderId = node.get("orderId").asLong();
            log.info("Refund notification received for order {}", orderId);
        } catch (Exception e) {
            log.error("Error processing REFUND_PROCESSED notification: {}", e.getMessage());
        }
    }
}
