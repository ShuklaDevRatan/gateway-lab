package com.drs.gateway_service.filter;

import com.drs.gateway_service.client.UserServiceClient;
import feign.FeignException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ApiKeyAuthenticationFilter implements Filter {

    private final UserServiceClient userServiceClient;
    public ApiKeyAuthenticationFilter(
            UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String apiKey = request.getHeader("X-API-KEY");

        if(apiKey == null || apiKey.isBlank()){

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("API Key is missing");
            return;
        }

        try {
            Boolean isValid =
                    userServiceClient.validateApiKey(apiKey);

            if (!Boolean.TRUE.equals(isValid)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid API Key");
                return;
            }

        } catch (FeignException e) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().write("Authentication service is unavailable");
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
