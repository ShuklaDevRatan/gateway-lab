package com.drs.gateway_service.client;


import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public UserServiceClient(
            WebClient.Builder webClientBuilder,
            RetryRegistry retryRegistry,
            CircuitBreakerRegistry circuitBreakerRegistry) {

        this.webClientBuilder = webClientBuilder;

        this.retry =
                retryRegistry.retry("user-service");

        this.circuitBreaker =
                circuitBreakerRegistry.circuitBreaker("user-service");
    }

    public Mono<Boolean> validateApiKey(String apiKey) {
        Mono<Boolean> webClientCall =
                webClientBuilder.build()
                        .get()
                        .uri("lb://user-service/internal/users/validate-api-key")
                        .header("X-API-KEY", apiKey)
                        .retrieve()
                        .bodyToMono(Boolean.class);

        Mono<Boolean> retryableCall =
                webClientCall.transformDeferred(
                        RetryOperator.of(retry)
                );

        return retryableCall.transformDeferred(
                CircuitBreakerOperator.of(circuitBreaker)
        );
    }
}