package com.ecommerce.authservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declares the Kafka topics this service produces to. Spring Kafka's
 * KafkaAdmin will create them on startup if they don't already exist
 * on the local broker (no manual `kafka-topics.sh` step required).
 */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name("user-events")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
