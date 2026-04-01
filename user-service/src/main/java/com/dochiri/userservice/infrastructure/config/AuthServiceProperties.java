package com.dochiri.userservice.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services.auth-service")
public record AuthServiceProperties(
        String baseUrl
) {
}
