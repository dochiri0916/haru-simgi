package com.dochiri.kafka.autoconfigure;

import com.dochiri.kafka.properties.KafkaTopicProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.List;

@Configuration(proxyBeanMethods = false)
class KafkaTopicAutoConfiguration {

    @Bean
    @ConditionalOnBean(KafkaAdmin.class)
    @ConditionalOnProperty(prefix = "dochiri.kafka", name = "topics[0].name")
    KafkaAdmin.NewTopics kafkaTopics(KafkaTopicProperties properties) {
        List<NewTopic> topics = properties.topics().stream()
                .map(topic -> new NewTopic(topic.name(), topic.partitions(), topic.replicas()))
                .toList();

        return new KafkaAdmin.NewTopics(topics.toArray(NewTopic[]::new));
    }

}