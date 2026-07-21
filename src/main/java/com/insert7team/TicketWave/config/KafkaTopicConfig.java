package com.insert7team.TicketWave.config;

import com.insert7team.TicketWave.common.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name(KafkaTopics.ORDER_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic orderCompletedTopic() {
        return TopicBuilder.name(KafkaTopics.ORDER_COMPLETED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic paymentCompletedTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_COMPLETED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic refundProcessedTopic() {
        return TopicBuilder.name(KafkaTopics.REFUND_PROCESSED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic eventChangedTopic() {
        return TopicBuilder.name(KafkaTopics.EVENT_CHANGED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic eventCancelledTopic() {
        return TopicBuilder.name(KafkaTopics.EVENT_CANCELLED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic notificationSendTopic() {
        return TopicBuilder.name(KafkaTopics.NOTIFICATION_SEND).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic fraudCheckTopic() {
        return TopicBuilder.name(KafkaTopics.FRAUD_CHECK).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic fraudAlertTopic() {
        return TopicBuilder.name(KafkaTopics.FRAUD_ALERT).partitions(3).replicas(1).build();
    }
}
