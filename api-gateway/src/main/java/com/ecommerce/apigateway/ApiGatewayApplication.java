package com.ecommerce.apigateway;

import com.ecommerce.apigateway.config.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point for the API Gateway.
 * This is the single entry point for the React frontend: it routes
 * requests to the correct downstream microservice via Eureka-based
 * load balancing, validates JWTs on protected routes, and applies
 * CORS + rate limiting before anything reaches a business service.
 */
@SpringBootApplication
@EnableConfigurationProperties(SecurityProperties.class)
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
