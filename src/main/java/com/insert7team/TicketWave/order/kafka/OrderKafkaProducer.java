package com.insert7team.TicketWave.order.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insert7team.TicketWave.shared.infrastructure.messaging.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderKafkaProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderKafkaProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaTopics.ORDER_CREATED, String.valueOf(event.orderId()), message);
            log.info("Published ORDER_CREATED for order {}", event.orderId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize OrderCreatedEvent", e);
        }
    }

    public void publishOrderCompleted(OrderCompletedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaTopics.ORDER_COMPLETED, String.valueOf(event.orderId()), message);
            log.info("Published ORDER_COMPLETED for order {}", event.orderId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize OrderCompletedEvent", e);
        }
    }
}
