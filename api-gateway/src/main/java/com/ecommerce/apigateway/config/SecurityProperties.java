package com.ecommerce.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Binds "security.public-endpoints" from application.yml / Config Server —
 * the list of Ant-style path patterns that do NOT require a JWT.
 */
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private List<String> publicEndpoints = List.of();

    public List<String> getPublicEndpoints() {
        return publicEndpoints;
    }

    public void setPublicEndpoints(List<String> publicEndpoints) {
        this.publicEndpoints = publicEndpoints;
    }
}
