package com.dochiri.configserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "config-server.security")
public record ConfigServerSecurityProperties(
        String username,
        String password
) {
    public ConfigServerSecurityProperties {
        username = username == null || username.isBlank() ? "adbin" : username;
        password = password == null || password.isBlank() ? "adbin" : password;
    }
}