package com.drs.gateway_service.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(
            ServerWebExchange exchange,
            Throwable ex) {

        if (ex instanceof AuthenticationServiceUnavailableException) {

            exchange.getResponse()
                    .setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);

            exchange.getResponse()
                    .getHeaders()
                    .setContentType(MediaType.APPLICATION_JSON);

            ErrorResponse errorResponse =
                    new ErrorResponse(
                            503,
                            "Authentication service is unavailable"
                    );

            try {

                byte[] bytes =
                        objectMapper.writeValueAsBytes(errorResponse);

                DataBuffer buffer =
                        exchange.getResponse()
                                .bufferFactory()
                                .wrap(bytes);

                return exchange.getResponse()
                        .writeWith(Mono.just(buffer));

            } catch (JsonProcessingException e) {

                return Mono.error(e);
            }
        }

        return Mono.error(ex);
    }
}