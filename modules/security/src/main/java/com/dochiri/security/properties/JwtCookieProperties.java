package com.dochiri.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt.cookie")
public record JwtCookieProperties(
        String accessTokenName,
        String refreshTokenName,
        String accessTokenPath,
        String refreshTokenPath,
        String domain,
        String sameSite,
        boolean secure
) {
    public JwtCookieProperties {
        accessTokenName = normalize(accessTokenName, "access_token");
        refreshTokenName = normalize(refreshTokenName, "refresh_token");
        accessTokenPath = normalize(accessTokenPath, "/");
        refreshTokenPath = normalize(refreshTokenPath, "/api/auth");
        sameSite = normalize(sameSite, "Lax");
        domain = normalizeNullable(domain);
    }

    private static String normalize(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    private static String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
