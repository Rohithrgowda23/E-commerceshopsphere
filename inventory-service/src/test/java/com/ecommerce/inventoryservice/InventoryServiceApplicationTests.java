package com.ecommerce.inventoryservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"order-events", "order-events.DLT", "inventory-events"})
class InventoryServiceApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the application context (JPA, Kafka listener, Security)
        // starts successfully against an in-memory H2 database and an
        // embedded Kafka broker.
    }
}
