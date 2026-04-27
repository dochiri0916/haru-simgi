package com.dochiri.authservice.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "auth.guest-session")
public record GuestSessionProperties(
        int expirationDays
) {

    public GuestSessionProperties {
        if (expirationDays <= 0) {
            throw new IllegalArgumentException("auth.guest-session.expiration-days는 1 이상이어야 합니다.");
        }
    }

    public Duration expiration() {
        return Duration.ofDays(expirationDays);
    }
}
