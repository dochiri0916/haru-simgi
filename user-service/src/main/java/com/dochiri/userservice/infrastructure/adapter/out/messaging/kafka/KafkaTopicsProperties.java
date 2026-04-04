package com.dochiri.userservice.infrastructure.adapter.out.messaging.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.topics")
public record KafkaTopicsProperties(
        String userRegistered
) {
}