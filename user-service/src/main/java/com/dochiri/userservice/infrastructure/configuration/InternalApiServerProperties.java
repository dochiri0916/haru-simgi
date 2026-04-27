package com.dochiri.userservice.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static java.util.Objects.requireNonNull;

@ConfigurationProperties(prefix = "internal-api.server")
public record InternalApiServerProperties(String token) {

    public InternalApiServerProperties {
        requireNonNull(token, "internal-api.server.token 설정이 필요합니다.");
        if (token.isBlank()) {
            throw new IllegalArgumentException("internal-api.server.token 설정이 비어 있을 수 없습니다.");
        }
    }
}
