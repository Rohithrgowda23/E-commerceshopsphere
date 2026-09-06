package com.ecommerce.productservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProductServiceApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the application context (JPA, caching, Security) starts
        // successfully against an in-memory H2 database with simple
        // in-memory caching (no Redis required for this test).
    }
}
