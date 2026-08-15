package com.drs.gateway_service.exception;

public class AuthenticationServiceUnavailableException
        extends RuntimeException {

    public AuthenticationServiceUnavailableException(String message) {
        super(message);
    }
}