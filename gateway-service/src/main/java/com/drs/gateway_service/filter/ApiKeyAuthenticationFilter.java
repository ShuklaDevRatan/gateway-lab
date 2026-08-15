package com.drs.gateway_service.filter;

import com.drs.gateway_service.client.UserServiceClient;
import com.drs.gateway_service.exception.AuthenticationServiceUnavailableException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

import java.io.IOException;
import java.util.concurrent.Callable;

public class ApiKeyAuthenticationFilter implements Filter {

    private final UserServiceClient userServiceClient;
    private final CircuitBreakerFactory circuitBreakerFactory;
    private final RetryRegistry retryRegistry;


    public ApiKeyAuthenticationFilter(
            UserServiceClient userServiceClient,
            CircuitBreakerFactory circuitBreakerFactory,
            RetryRegistry retryRegistry) {

        this.userServiceClient = userServiceClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.retryRegistry = retryRegistry;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;


        String apiKey = request.getHeader("X-API-KEY");

        CircuitBreaker circuitBreaker =
                circuitBreakerFactory.create("user-service");

        Retry retry = retryRegistry.retry("user-service");



        if(apiKey == null || apiKey.isBlank()){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("API Key is missing");
            return;
        }

        try {

            Callable<Boolean> retryableCall =
                    Retry.decorateCallable(
                            retry,
                            () -> userServiceClient.validateApiKey(apiKey)
                    );

            Boolean isValid =
                    circuitBreaker.run(
                            () -> {
                                try {
                                    return retryableCall.call();
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            },
                            throwable -> {
                                throw new AuthenticationServiceUnavailableException(
                                        "Authentication service is unavailable"
                                );
                            }
                    );

            if (!Boolean.TRUE.equals(isValid)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid API Key");
                return;
            }

        } catch (AuthenticationServiceUnavailableException e) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().write(e.getMessage());
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
