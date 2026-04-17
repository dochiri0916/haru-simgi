package com.dochiri.time.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.ZoneId;

import static java.util.Objects.requireNonNull;

/**
 * 공통 시간 설정을 제공한다.
 */
@ConfigurationProperties(prefix = "time")
public record TimeProperties(
        @DefaultValue("Asia/Seoul") ZoneId timezone
) {
    public TimeProperties {
        requireNonNull(timezone);
    }
}
