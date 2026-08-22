package com.drs.gateway_service.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {

        return builder.routes()

                .route("user-service", route -> route
                        .path("/gateway/api/users/**")
                        .filters(filter -> filter
                                .stripPrefix(2)
                        )
                        .uri("http://localhost:8081")
                )

                .build();
    }
}