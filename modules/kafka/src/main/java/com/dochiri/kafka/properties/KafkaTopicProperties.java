package com.dochiri.kafka.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "dochiri.kafka")
public record KafkaTopicProperties(
        List<TopicProperties> topics
) {
    public KafkaTopicProperties {
        topics = topics == null ? List.of() : List.copyOf(topics);
    }

    public record TopicProperties(
            String name,
            Integer partitions,
            Short replicas
    ) {
        public TopicProperties {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Kafka topic name must not be blank.");
            }
            if (partitions == null || partitions < 1) {
                partitions = 1;
            }
            if (replicas == null || replicas < 1) {
                replicas = 1;
            }
        }
    }
}