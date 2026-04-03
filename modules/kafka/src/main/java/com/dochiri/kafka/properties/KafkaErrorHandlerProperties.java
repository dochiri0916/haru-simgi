package com.dochiri.kafka.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dochiri.kafka.error-handler")
public record KafkaErrorHandlerProperties(
        Boolean enabled,
        Long retryIntervalMs,
        Long maxAttempts,
        Boolean ackAfterHandle,
        DlqProperties dlq
) {
    public KafkaErrorHandlerProperties {
        enabled = enabled == null || enabled;
        retryIntervalMs = retryIntervalMs == null || retryIntervalMs < 0 ? 1000L : retryIntervalMs;
        maxAttempts = maxAttempts == null || maxAttempts < 1 ? 3L : maxAttempts;
        ackAfterHandle = ackAfterHandle != null && ackAfterHandle;
        dlq = dlq == null ? new DlqProperties(null, null) : dlq;
    }

    public record DlqProperties(
            Boolean enabled,
            String suffix
    ) {
        public DlqProperties {
            enabled = enabled != null && enabled;
            suffix = suffix == null || suffix.isBlank() ? ".DLQ" : suffix;
        }
    }
}