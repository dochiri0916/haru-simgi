package com.dochiri.authservice.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static java.util.Objects.requireNonNull;

@ConfigurationProperties(prefix = "internal-api.client")
public record InternalApiClientProperties(String token) {

    public InternalApiClientProperties {
        requireNonNull(token, "internal-api.client.token 설정이 필요합니다.");
        if (token.isBlank()) {
            throw new IllegalArgumentException("internal-api.client.token 설정이 비어 있을 수 없습니다.");
        }
    }
}
