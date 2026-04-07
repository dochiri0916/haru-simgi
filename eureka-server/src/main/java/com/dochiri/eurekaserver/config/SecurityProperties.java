package com.dochiri.eurekaserver.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "security")
public record SecurityProperties(
        @NotBlank(message = "Eureka Server username은 비어 있을 수 없습니다.")
        String username,
        @NotBlank(message = "Eureka Server password는 비어 있을 수 없습니다.")
        String password
) {
}