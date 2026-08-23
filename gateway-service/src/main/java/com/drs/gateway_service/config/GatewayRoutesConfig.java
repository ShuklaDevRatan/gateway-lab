package com.drs.gateway_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayRoutesConfig {

    @Value("${rate-limit.replenish-rate}")
    private int replenishRate;

    @Value("${rate-limit.burst-capacity}")
    private int burstCapacity;

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(replenishRate , burstCapacity);
    }

    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange ->
                Mono.justOrEmpty(
                        exchange.getRequest()
                                .getHeaders()
                                .getFirst("X-API-KEY")
                );
    }

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {

        return builder.routes()

                .route("user-service", route -> route
                        .path("/gateway/api/users/**")
                        .filters(filters ->
                                filters
                                        .stripPrefix(2)
                                        .requestRateLimiter(config -> config
                                                .setRateLimiter(redisRateLimiter())
                                                .setKeyResolver(apiKeyResolver())
                                        )
                        )
                        .uri("lb://user-service")
                )

                .build();
    }
}