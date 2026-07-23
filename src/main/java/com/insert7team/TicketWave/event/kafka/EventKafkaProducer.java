package com.insert7team.TicketWave.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insert7team.TicketWave.shared.infrastructure.messaging.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(EventKafkaProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public EventKafkaProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishEventChanged(EventChangedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaTopics.EVENT_CHANGED, String.valueOf(event.eventEntityId()), message);
            log.info("Published EVENT_CHANGED for event {}", event.eventEntityId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize EventChangedEvent", e);
        }
    }

    public void publishEventCancelled(EventCancelledEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaTopics.EVENT_CANCELLED, String.valueOf(event.eventEntityId()), message);
            log.info("Published EVENT_CANCELLED for event {}", event.eventEntityId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize EventCancelledEvent", e);
        }
    }
}
