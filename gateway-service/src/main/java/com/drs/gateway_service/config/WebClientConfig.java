package com.drs.gateway_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    public WebClientConfig() {
    }

    @Bean
    public WebClient.Builder webClientBuilder(
            @Value("${user-service.base-url}")String userServiceBaseUrl) {


        return WebClient.builder()
                .baseUrl(userServiceBaseUrl);
    }
}
