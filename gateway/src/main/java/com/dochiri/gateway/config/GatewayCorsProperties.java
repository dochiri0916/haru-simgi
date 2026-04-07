package com.dochiri.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "cors")
public record GatewayCorsProperties(
        List<String> allowedOrigins,
        boolean allowCredentials
) {
    public GatewayCorsProperties {
        if (allowedOrigins == null) {
            allowedOrigins = List.of();
        }
    }
}