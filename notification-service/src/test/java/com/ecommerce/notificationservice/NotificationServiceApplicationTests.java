package com.ecommerce.notificationservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {
        "user-events", "user-events.DLT",
        "order-events", "order-events.DLT",
        "payment-events", "payment-events.DLT",
        "inventory-events", "inventory-events.DLT"})
class NotificationServiceApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the application context (JPA, four Kafka listeners,
        // Security) starts successfully against an in-memory H2 database
        // and an embedded Kafka broker.
    }
}
