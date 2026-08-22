package com.drs.user_service.controller;

import com.drs.user_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
public class InternalAuthController {

    private final UserService userService;

    public InternalAuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/validate-api-key")
    public ResponseEntity<Boolean> validateApiKey(@RequestHeader("X-API-KEY") String apiKey){
        System.out.println("User Service received validation request");
        System.out.println("Received API Key: " + apiKey);
        return ResponseEntity.ok(userService.isValidApiKey(apiKey));
    }
}
