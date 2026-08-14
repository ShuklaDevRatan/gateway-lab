package com.drs.gateway_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "user-service",
        url = "http://localhost:8081"
)
public interface UserServiceClient {

    @GetMapping("/internal/users/validate-api-key")
    Boolean validateApiKey(
            @RequestHeader("X-API-KEY") String apiKey
    );

}