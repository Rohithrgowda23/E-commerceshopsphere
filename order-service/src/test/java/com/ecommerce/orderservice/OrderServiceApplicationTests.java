package com.ecommerce.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {
        "order-events", "payment-events", "payment-events.DLT",
        "inventory-events", "inventory-events.DLT"})
class OrderServiceApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the application context (JPA, OpenFeign, Kafka
        // listeners, Security) starts successfully against an in-memory
        // H2 database and an embedded Kafka broker.
    }
}
