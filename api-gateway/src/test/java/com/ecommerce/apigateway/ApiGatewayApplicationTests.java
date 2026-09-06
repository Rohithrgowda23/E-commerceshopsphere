package com.ecommerce.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the gateway application context starts successfully
        // with the "test" profile (Config Server import disabled — see
        // application-test.yml), including all filters and routes.
    }
}
