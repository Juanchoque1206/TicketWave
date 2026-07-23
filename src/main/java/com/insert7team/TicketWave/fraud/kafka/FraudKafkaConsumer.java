package com.insert7team.TicketWave.fraud.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insert7team.TicketWave.shared.infrastructure.messaging.KafkaTopics;
import com.insert7team.TicketWave.fraud.service.FraudDetectionService;
import com.insert7team.TicketWave.order.dto.CreateOrderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FraudKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(FraudKafkaConsumer.class);

    private final FraudDetectionService fraudDetectionService;
    private final ObjectMapper objectMapper;

    public FraudKafkaConsumer(FraudDetectionService fraudDetectionService, ObjectMapper objectMapper) {
        this.fraudDetectionService = fraudDetectionService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "ticketwave-fraud")
    public void onOrderCreated(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            Long userId = node.get("userId").asLong();
            Long orderId = node.get("orderId").asLong();
            int itemCount = node.get("itemCount").asInt();
            log.info("Fraud check triggered for order {} by user {} ({} items)", orderId, userId, itemCount);
        } catch (Exception e) {
            log.error("Error processing ORDER_CREATED for fraud check: {}", e.getMessage());
        }
    }
}
