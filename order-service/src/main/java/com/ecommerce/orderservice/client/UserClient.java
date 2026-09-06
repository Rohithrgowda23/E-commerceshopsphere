package com.ecommerce.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", configuration = com.ecommerce.orderservice.config.FeignConfig.class)
public interface UserClient {

    @GetMapping("/api/users/{id}")
    UserClientResponse getUser(@PathVariable("id") String id);
}
