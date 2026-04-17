package com.dochiri.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "auth.session.redis.key-prefix")
public record AuthSessionRedisKeyProperties(
        String session,
        String refresh,
        String userSessions
) {

    public AuthSessionRedisKeyProperties {
        session = normalize(session, "auth:session:");
        refresh = normalize(refresh, "auth:refresh:");
        userSessions = normalize(userSessions, "auth:user-sessions:");
    }

    private static String normalize(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
