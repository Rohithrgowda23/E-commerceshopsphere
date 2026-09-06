package com.ecommerce.userservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"user-events", "user-events.DLT"})
class UserServiceApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the application context (JPA, Kafka listener, Security)
        // starts successfully against an in-memory H2 database and an
        // embedded Kafka broker.
    }
}
