package com.drs.gateway_service.filter;

import com.drs.gateway_service.client.UserServiceClient;
import com.drs.gateway_service.exception.AuthenticationServiceUnavailableException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
public class ApiKeyAuthenticationFilter implements GlobalFilter {

    private final UserServiceClient userServiceClient;

    public ApiKeyAuthenticationFilter(
            UserServiceClient userServiceClient) {

        this.userServiceClient = userServiceClient;
    }


    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {
        String apiKey = exchange.getRequest()
                .getHeaders()
                .getFirst("X-API-KEY");

        ServerHttpResponse response =
                exchange.getResponse();

        if (apiKey == null || apiKey.isBlank()) {

            response.setStatusCode(HttpStatus.UNAUTHORIZED);

            return response.setComplete();
        }

        // authentication check yahan aayega
        System.out.println("API Key Filter: " + apiKey);
        Mono<Boolean> validationResult =
                userServiceClient.validateApiKey(apiKey);
        return validationResult.flatMap(isValid -> {
            if (Boolean.TRUE.equals(isValid)) {
                return chain.filter(exchange);
            }
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }).onErrorMap(error ->
                new AuthenticationServiceUnavailableException(
                        "Authentication service is unavailable"
                )
        );
    }
}
