package com.dochiri.configserver.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "config-server.security")
public record SecurityProperties(
        @NotBlank(message = "Config Server username은 비어 있을 수 없습니다.")
        String username,
        @NotBlank(message = "Config Server password는 비어 있을 수 없습니다.")
        String password
) {}
