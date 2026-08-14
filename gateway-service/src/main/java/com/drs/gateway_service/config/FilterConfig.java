package com.drs.gateway_service.config;

import com.drs.gateway_service.client.UserServiceClient;
import com.drs.gateway_service.filter.ApiKeyAuthenticationFilter;
import com.drs.gateway_service.filter.GatewayRequestLoggingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<GatewayRequestLoggingFilter> gatewayRequestLoggingFilter(){
        FilterRegistrationBean<GatewayRequestLoggingFilter> registrationBean =
                new FilterRegistrationBean<>();
        registrationBean.setFilter(new GatewayRequestLoggingFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<ApiKeyAuthenticationFilter> apiKeyAuthenticationFilter(UserServiceClient userServiceClient){
        FilterRegistrationBean<ApiKeyAuthenticationFilter> registrationBean =
                new FilterRegistrationBean<>();
        registrationBean.setFilter(new ApiKeyAuthenticationFilter(userServiceClient ));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(2);
        return registrationBean;
    }
}
