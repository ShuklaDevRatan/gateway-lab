package com.drs.gateway_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gateway/api")
public class HealthStatus {

    @GetMapping("/health")
    public String healthCheck(){
        return "Gateway service running.....";
    }
}
