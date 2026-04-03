package com.dochiri.authservice.infrastructure.adapter.in.messaging.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.topics")
public record KafkaTopicsProperties(
        String userRegistered
) {
}
