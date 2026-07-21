package com.insert7team.TicketWave.payment.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insert7team.TicketWave.common.kafka.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentKafkaProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PaymentKafkaProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaTopics.PAYMENT_COMPLETED, String.valueOf(event.orderId()), message);
            log.info("Published PAYMENT_COMPLETED for order {}", event.orderId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize PaymentCompletedEvent", e);
        }
    }

    public void publishRefundProcessed(RefundProcessedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaTopics.REFUND_PROCESSED, String.valueOf(event.orderId()), message);
            log.info("Published REFUND_PROCESSED for order {}", event.orderId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize RefundProcessedEvent", e);
        }
    }
}
