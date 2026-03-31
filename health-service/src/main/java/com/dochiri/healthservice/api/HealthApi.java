package com.dochiri.healthservice.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthApi {

    @GetMapping
    public String health() {
        return "health-service is running!";
    }

}