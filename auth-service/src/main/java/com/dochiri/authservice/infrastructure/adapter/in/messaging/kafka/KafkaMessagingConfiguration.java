package com.dochiri.authservice.infrastructure.adapter.in.messaging.kafka;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
public class KafkaMessagingConfiguration {

    @Bean
    ConsumerFactory<String, UserRegisteredMessage> userRegisteredConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}") String groupId
    ) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        JacksonJsonDeserializer<UserRegisteredMessage> valueDeserializer =
                new JacksonJsonDeserializer<>(UserRegisteredMessage.class);
        valueDeserializer.addTrustedPackages(UserRegisteredMessage.class.getPackageName());
        valueDeserializer.ignoreTypeHeaders();

        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                valueDeserializer
        );
    }
}
